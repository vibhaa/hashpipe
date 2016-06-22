# TLV parsing of IPv4 options

## Description

This program illustrates how to parse IPv4 options with bmv2. There is a very
easy way to parse IPv4 options in P4 using a single variable length
field. However, this means that options are not parsed individually, but
together, as one block. All the options are parsed to a single field, which is
fine for many use cases but can be insufficient in some. In this example, we use
TLV parsing to parse all options separately to their own header instance.

The program is quite straightforward. The following IPv4 options are supported:

- end of list
- no-op
- security
- timestamp

There is one important caveat: when compiling the P4 program, a strict ordering
of all packet headers has to be known. This is usually done by inspecting the
parse graph and running a topological sorting algorithm on it. However this
algorithm will not work if there exists loops in the header graph, as is the
case with TLV parsing. There is not yet an official way of enforcing your own
header ordeing in the P4 program, so we had to bypass this restriction by using
a `@pragma`, as you can see in the code:

    @pragma header_ordering ethernet ipv4_base ipv4_option_security ipv4_option_NOP ipv4_option_timestamp ipv4_option_EOL

This `@pragma` instruction will be interpreted by the P4 -> bmv2 compiler.

This order is used by the deparser, when sending a packet out of the egress
port, which means that the option layout for the outgoing packet may not be the
same as for the incoming packet.

The table `format_options` makes sure that the IPv4 header is formatted
correctly in the outgoing packet.

Note that the P4 program assumes the incoming packet is correctly formatted. We
do not perform any sanity checking because *parser execptions* are not yet
supported by bmv2.

So in a nutshell, all this P4 program does is:

1. parse the IPv4 options for the incoming packet
2. re-serialize the packet again, with a potentially different order for options

### Running the demo

We provide a small demo to let you test the program. It consists of the
following scripts:
- [run_switch.sh] (run_switch.sh): compile the P4 program and starts the switch,
  also configures the data plane by running the CLI [commands] (commands.txt).
- [send_one.py] (send_one.py): send an IPv4 packet with options

To run the demo:
- start the switch and configure the tables: `sudo ./run_switch.sh`.
- run the Python script: `sudo python send_one.py`.

Then inspect the `pcap` file for port 0 of the switch (`veth0.pcap`) with
Wireshark. You will observe that the order of the IPv4 options has changed but
that the outgoing packet contains all the options of the incoming packet and is
perfectly valid (with a correct checksum).
