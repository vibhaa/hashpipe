field_list flow{
	ipv4.srcAddr;
	//ipv4.dstAddr;
	//tcp.srcPort;
	//tcp.dstPort;
	//ipv4.protocol;
}

#define TABLE_SIZE 10
#define TABLE_IDX_WIDTH 15

/*****************************************************************************/
/* header definition                                                         */
/*****************************************************************************/

header_type perStage_metadata_t{
	fields{
		h : TABLE_IDX_WIDTH;
		minIdx: TABLE_IDX_WIDTH;
		minVal: 32;
		valCur	  : 32;
		swapSpace : 32;
	}
}

metadata perStage_metadata_t d_meta;

/*****************************************************************************/
/* common to all stages                                                      */
/*****************************************************************************/

register flow_id{
	width:32;
	instance_count: TABLE_SIZE;
}

register flow_count{
    width : 32;
    instance_count : TABLE_SIZE;
}

/*****************************************************************************/
/* stage 1                                                     */
/*****************************************************************************/
/*field_list_calculation stage1_hash1{
	input{
		flow;
	}
	algorithm: my_hash1;
	output_width: TABLE_IDX_WIDTH;
}*/

action stage1_read(){
	//modify_field_with_hash_based_offset(d_meta.h, 0, stage1_hash1, TABLE_SIZE);
	//register_read(d_meta.valCur, flow_count, d_meta.h);
    //modify_field(d_meta.valCur, 5);
    register_read(d_meta.valCur, flow_count, 1);
}

table stage1 {
	actions {
		stage1_read;
	}
}

/*****************************************************************************/
/* common actions                                                            */
/*****************************************************************************/
action swapMin(){
	modify_field(d_meta.minVal, d_meta.valCur);
	//modify_field(d_meta.minIdx, d_meta.h);
    modify_field(d_meta.minIdx, 2);
}

table changeMin{
	actions {
		swapMin;
	}
}

action overWrite(){
	//register_write(flow_id, d_meta.minIdx, ipv4.srcAddr);
    register_write(flow_id, d_meta.minIdx, 54);
	register_write(flow_count, d_meta.minIdx, 1);
}

table insertEntry{
	actions {
		overWrite;
	}
}