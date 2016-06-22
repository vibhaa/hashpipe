/*****************************************************************************/
/* flow size stats                                                                 */
/*****************************************************************************/
counter size {
    type : packets;
    instance_count : HASH_TABLE_SIZE;
}

action size_update() {
    count(size, hash_index);
}

table hash_table_update {
    reads {
        hash_index : exact;
    }
    actions {
        size_update;
    }
    size : HASH_TABLE_SIZE;
}

control process_ingress_flows {
    apply(hash_table_update);
}