/*
Copyright 2013-present Barefoot Networks, Inc. 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#include "includes/headers.p4"
#include "includes/parser.p4"
#include "includes/intrinsic.p4"

#define ECMP_BIT_WIDTH 10
#define ECMP_GROUP_TABLE_SIZE 1024
#define ECMP_NHOP_TABLE_SIZE 16384
#define NUM_FLOW_IDS 1024
#define FLOW_IDS_BITS 10

header_type ingress_metadata_t {
    fields {
        ecmp_offset : 14; // offset into the ecmp table

        nhop_ipv4 : 32;

        // TODO: add your flowlet metadata here
        flow_id: FLOW_IDS_BITS;
        flowlet_id : 32;
        flowlet_gap : 64; // inter-packet gap
        last_seen: 64;
    }
}

metadata ingress_metadata_t ingress_metadata;

register last_seen {
    width: 64;
    instance_count : NUM_FLOW_IDS;
}

register flowlet_ids {
    width: 32;
    instance_count : NUM_FLOW_IDS;
}

action _drop() {
    drop();
}

action set_nhop(nhop_ipv4, port) {
    modify_field(ingress_metadata.nhop_ipv4, nhop_ipv4);
    modify_field(standard_metadata.egress_spec, port);
    add_to_field(ipv4.ttl, -1);
}

// TODO: add flowlet id to hash fields

field_list l3_hash_fields {
    ipv4.srcAddr;
    ipv4.dstAddr;
    ipv4.protocol;
    tcp.srcPort;
    tcp.dstPort;
}

field_list flowlet_hash_fields {
    ipv4.srcAddr;
    ipv4.dstAddr;
    ipv4.protocol;
    tcp.srcPort;
    tcp.dstPort;
    ingress_metadata.flowlet_id;
    //intrinsic_metadata.ingress_global_timestamp;
}

field_list_calculation ecmp_hash {
    input {
        l3_hash_fields;
    }
    algorithm : crc16;
    output_width : ECMP_BIT_WIDTH;
}

field_list_calculation flowlet_hash {
    input {
        flowlet_hash_fields;
    }
    algorithm: crc16;
    output_width : ECMP_BIT_WIDTH;
}

action populate_flowlet_parameters() {
    modify_field_with_hash_based_offset(ingress_metadata.flow_id,
      0, ecmp_hash, NUM_FLOW_IDS);
    register_read(ingress_metadata.flowlet_id, flowlet_ids,
      ingress_metadata.flow_id);
    register_read(ingress_metadata.last_seen, last_seen,
      ingress_metadata.flow_id);
    register_write(last_seen, ingress_metadata.flow_id,
      intrinsic_metadata.ingress_global_timestamp);
    modify_field(ingress_metadata.flowlet_gap, 
      intrinsic_metadata.ingress_global_timestamp - ingress_metadata.last_seen);
}

action set_ecmp_select(ecmp_base, ecmp_count) {
    modify_field_with_hash_based_offset(ingress_metadata.ecmp_offset, ecmp_base,
                                        flowlet_hash, ecmp_count);
}

// for new flowlet: update both the register value as well as current packet
// metadata with the new flowlet id
action update_new_flowlet_action() {
    register_write(flowlet_ids, ingress_metadata.flow_id, 
      ingress_metadata.flowlet_id + 1);
    modify_field(ingress_metadata.flowlet_id, ingress_metadata.flowlet_id + 1);
}

table flowlet_set {
    reads {
        ipv4.dstAddr : lpm;
    }
    actions {
        _drop;
        populate_flowlet_parameters;
    }
}

table update_new_flowlet {
    actions {
        update_new_flowlet_action;
    }
}

table ecmp_group {
    reads {
        ipv4.dstAddr : lpm;
    }
    actions {
        _drop;
        set_ecmp_select;
    }
    size : ECMP_GROUP_TABLE_SIZE;
}

table ecmp_nhop {
    reads {
        ingress_metadata.ecmp_offset : exact;
    }
    actions {
        _drop;
        set_nhop;
    }
    size : ECMP_NHOP_TABLE_SIZE;
}

action set_dmac(dmac) {
    modify_field(ethernet.dstAddr, dmac);
}

table forward {
    reads {
        ingress_metadata.nhop_ipv4 : exact;
    }
    actions {
        set_dmac;
        _drop;
    }
    size: 512;
}

action rewrite_mac(smac) {
    modify_field(ethernet.srcAddr, smac);
}

table send_frame {
    reads {
        standard_metadata.egress_port: exact;
    }
    actions {
        rewrite_mac;
        _drop;
    }
    size: 256;
}

control ingress {
    // TODO: flowlet switching
    apply(flowlet_set);
    // if ((intrinsic_metadata.ingress_global_timestamp -
    //        ingress_metadata.last_seen) 
    //    > 50000) {
    if (ingress_metadata.flowlet_gap > 100) {
        apply(update_new_flowlet);
    }
    apply(ecmp_group);
    apply(ecmp_nhop);
    apply(forward);
}

control egress {
    apply(send_frame);
}
