action do_stage1(){
    // first table stage
    hh_meta.mKeyCarried = ipv4.srcAddr;
    hh_meta.mCountCarried = 0;

    // hash using my custom function 
    modify_field_with_hash_based_offset(hh_meta.mIndex, 0, stage1_hash, 32);

    // read the key and value at that location
    hh_meta.mKeyInTable = flow_tracker[hh_meta.mIndex];
    hh_meta.mCountInTable = packet_counter[hh_meta.mIndex];
    hh_meta.mValid = valid_bit[hh_meta.mIndex];

    // check if location is empty or has a differentkey in there
    hh_meta.mKeyInTable = (hh_meta.mValid == 0)? hh_meta.mKeyCarried : hh_meta.mKeyInTable;
    hh_meta.mDiff = (hh_meta.mValid == 0)? 0 : hh_meta.mKeyInTable - hh_meta.mKeyCarried;

    // update hash table
    flow_tracker[hh_meta.mIndex] = ipv4.srcAddr;
    packet_counter[hh_meta.mIndex] = ((hh_meta.mDiff == 0)?
    hh_meta.mCountInTable + 1 : 1);
    valid_bit[hh_meta.mIndex] = 1;

    // update metadata carried to the next table stage
    hh_meta.mKeyCarried = ((hh_meta.mDiff == 0) ? 0: hh_meta.mKeyInTable);
    hh_meta.mCountCarried = ((hh_meta.mDiff == 0) ? 0: hh_meta.mCountInTable);  
}

