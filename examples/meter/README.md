# Meter

## Description

This program illustrates as simply as possible how to use meters in P4 with
bmv2. bmv2 uses two-rate three-color meters as described [here]
(https://tools.ietf.org/html/rfc2698).

For each incoming packet the `m_table` table is applied and the appropriate
meter (based on the packet's source MAC address) is executed. Based on the
observed traffic rate for this sender and the meter's configuration, executing
the meter will yield one of 3 values: `0` (*GREEN*), `1` (*YELLOW*) or `2`
(*RED*). This value will be copied to metadata field `meta.meter_tag`. Note that
if no meter was associated to the sender's MAC address, the table will be a
no-op. This table also redirects all packets - with a known source MAC address-
to port 2 of the switch.

After that, the packet will go through a second table, `m_filter`, which can
either be a no-op or drop the packet based on how the packet was tagged by the
meter. If you take a look at the [runtime commands] (commands.txt) we wrote for
this example, you will see that we configure the table to drop all the packets
for which the color is not *GREEN* (i.e. all packets for which `meta.meter_tag`
is not `0`).

The [commands.txt] (commands.txt) file also gives you the meter
configuration. In this case, the first rate is 0.5 packets per second, with a
burst size of 1, and the second rate is 10 packets per second, with a burst size
of 1 also. Feel free to play with the numbers, but these play nicely with the
demonstration below.

Note that we use an `indirect` meter array, because `direct` ones are not
supported yet by bmv2.

### Running the demo

We provide a small demo to let you test the program. It consists of the
following scripts:
- [run_switch.sh] (run_switch.sh): compile the P4 program and starts the switch,
  also configures the data plane by running the CLI [commands] (commands.txt).
- [send_and_receive.py] (send_and_receive.py): send packets periodically on port
  0 and listen for packets on port 2.

To run the demo:
- start the switch and configure the tables and the meters: `sudo
  ./run_switch.sh`.
- run the Python script: `sudo python send_and_receive.py 1`. As you can see,
  the script takes one argument, which is the time interval (in seconds) between
  two consecutive packets.

If you run the script with an interval of one second, you should observe the
following output:

    Received one
    Sent one
    Sent one
    Received one
    Sent one
    Sent one
    Received one
    Sent one
    ...

This is because we send one packet every second, while the first rate of the
meter is 0.5 packets per second. The P4 program therefore drops on average one
packet out of two.
