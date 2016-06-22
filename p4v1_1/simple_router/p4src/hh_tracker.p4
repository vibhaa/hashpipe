field_list hash_list {
    ipv4.srcAddr;
    //ipv4.dstAddr;
}

field_list_calculation stage1_hash {
    input {
        hash_list;
    }
    algorithm : crc16;
    output_width : 10;
}

header_type tracking_metadata_t {
    fields {
        bit<32> mKeyInTable;
        bit<32> mCountInTable;
        bit<10> mIndex;
        bit<1> mValid;
        bit<32> mKeyCarried;
        bit<32> mCountCarried;
        bit<32> mSwapSpace;
    }
}

metadata tracking_metadata_t track_meta;

register flow_tracker_stage1 {
    width: 32;
    static: track_stage1;
    instance_count: 1024;
}

register packet_counter_stage1 {
    width: 32;
    static: track_stage1;
    instance_count: 1024;
}

register valid_bit_stage1 {
    width: 1;
    static: track_stage1;
    instance_count: 1024;
}

action do_stage1(){
    // first table stage
    track_meta.mKeyCarried = ipv4.srcAddr;
    track_meta.mCountCarried = 0;

    // hash using my custom function 
    modify_field_with_hash_based_offset(track_meta.mIndex, 0, stage1_hash, 1024);

    // read the key and value at that location
    track_meta.mKeyInTable = flow_tracker_stage1[track_meta.mIndex];
    track_meta.mCountInTable = packet_counter_stage1[track_meta.mIndex];
    track_meta.mValid = valid_bit_stage1[track_meta.mIndex];

    // check if location is empty or has a differentkey in there
    track_meta.mKeyInTable = (track_meta.mValid == 0)? track_meta.mKeyCarried : track_meta.mKeyInTable;
    track_meta.mSwapSpace = track_meta.mKeyInTable - track_meta.mKeyCarried;

    // update hash table
    flow_tracker_stage1[track_meta.mIndex] = ipv4.srcAddr; //track_meta.mKeyCarried;
    packet_counter_stage1[track_meta.mIndex] = ((track_meta.mSwapSpace == 0)?
    track_meta.mCountInTable + 1 : 1);
    valid_bit_stage1[track_meta.mIndex] = 1;

    // update metadata carried to the next table stage
    track_meta.mKeyCarried = ((track_meta.mSwapSpace == 0) ? 0:
    track_meta.mKeyInTable);
    track_meta.mCountCarried = ((track_meta.mSwapSpace == 0) ? 0:
    track_meta.mCountInTable);  
}

table track_stage1 {
    actions { do_stage1; }
    size: 0;
}       
