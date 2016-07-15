field_list hash_list {
    track_meta.mKeyCarried;
    //ipv4.srcAddr;
    //ipv4.dstAddr;
}

field_list_calculation stage1_hash {
    input {
        hash_list;
    }
    algorithm : my_hash_1;
    output_width : 10;
}

field_list_calculation stage2_hash {
    input {
        hash_list;
    }
    algorithm : my_hash_2;
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
        bit<32> mDiff;
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
    modify_field_with_hash_based_offset(track_meta.mIndex, 0, stage1_hash,
    1024);

    //flow_tracker_stage1[0] = track_meta.mIndex;

    // read the key and value at that location
    track_meta.mKeyInTable = flow_tracker_stage1[track_meta.mIndex];
    track_meta.mCountInTable = packet_counter_stage1[track_meta.mIndex];
    track_meta.mValid = valid_bit_stage1[track_meta.mIndex];

    // check if location is empty or has a differentkey in there
    track_meta.mKeyInTable = (track_meta.mValid == 0)? track_meta.mKeyCarried : track_meta.mKeyInTable;
    track_meta.mDiff = track_meta.mKeyInTable - track_meta.mKeyCarried;

    // update hash table
    flow_tracker_stage1[track_meta.mIndex] = ipv4.srcAddr; //track_meta.mKeyCarried;
    packet_counter_stage1[track_meta.mIndex] = ((track_meta.mDiff == 0)?
    track_meta.mCountInTable + 1 : 1);
    valid_bit_stage1[track_meta.mIndex] = 1;

    // update metadata carried to the next table stage
    track_meta.mKeyCarried = ((track_meta.mDiff == 0) ? 0:
    track_meta.mKeyInTable);
    track_meta.mCountCarried = ((track_meta.mDiff == 0) ? 0:
    track_meta.mCountInTable);  
}

table track_stage1 {
    actions { do_stage1; }
    size: 0;
}

/********************** table stage 2 **************************/

register flow_tracker_stage2 {
    width: 32;
    static: track_stage2;
    instance_count: 1024;
}

register packet_counter_stage2 {
    width: 32;
    static: track_stage2;
    instance_count: 1024;
}

register valid_bit_stage2 {
    width: 1;
    static: track_stage2;
    instance_count: 1024;
}

action do_stage2(){
    // hash using my custom function 
    modify_field_with_hash_based_offset(track_meta.mIndex, 0, stage2_hash,
    1024);

    //flow_tracker_stage2[0] = track_meta.mIndex;

    // read the key and value at that location
    track_meta.mKeyInTable = flow_tracker_stage2[track_meta.mIndex];
    track_meta.mCountInTable = packet_counter_stage2[track_meta.mIndex];
    track_meta.mValid = valid_bit_stage2[track_meta.mIndex];

    // check if location is empty or has a differentkey in there
    track_meta.mKeyInTable = (track_meta.mValid == 0)? track_meta.mKeyCarried : track_meta.mKeyInTable;
    track_meta.mDiff = track_meta.mKeyInTable - track_meta.mKeyCarried;

    // update hash table
    flow_tracker_stage2[track_meta.mIndex] = ((track_meta.mDiff == 0)?
    track_meta.mKeyInTable : ((track_meta.mCountCarried >=
    track_meta.mCountInTable) ? track_meta.mKeyCarried :
    track_meta.mKeyInTable));

    packet_counter_stage2[track_meta.mIndex] = ((track_meta.mDiff == 0)?
    track_meta.mCountInTable + track_meta.mCountCarried :
    ((track_meta.mCountCarried >= track_meta.mCountInTable) ?
    track_meta.mCountCarried : track_meta.mCountInTable));

    valid_bit_stage2[track_meta.mIndex] = ((track_meta.mValid == 0) ?
    ((track_meta.mKeyCarried == 0) ? (bit<1>)0 : 1) : (bit<1>)1);

    // update metadata carried to the next table stage
    track_meta.mKeyCarried = ((track_meta.mDiff == 0) ? 0:
    track_meta.mKeyInTable);
    track_meta.mCountCarried = ((track_meta.mDiff == 0) ? 0:
    track_meta.mCountInTable);  
}

table track_stage2 {
    actions { do_stage2; }
    size: 0;
}
       
