# Register

## Description

This program illustrates as simply as possible how to use registers in P4 and
read / write the register state using the bmv2 runtime CLI.

This will probably change in the future but as of now the data plane is not
doing anything (so don't try to send packets). However you can take a look at
the P4 source code and at the read_register and write_register scripts. You can
also run the following demo:

### Running the demo

To run the demo:
- start the switch with `./run_switch.sh`.
- write a register cell with `write_register.sh`. For example:
  `./write_register.sh 123 88` to set `my_register[123]` to 88.
- read a register cell with `read_register.sh`. For example:
  `./read_register.sh 123` to read `my_register[123]`.
