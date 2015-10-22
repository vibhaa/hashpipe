from scapy.all import *

p = Ether() / IP(options=IPOption('\x44\x0c\x05\x00\x01\x02\x03\x04\x05\x06\x07\x08') / IPOption('\x82\x0b\xa1\xa2\xa3\xa4\xa5\xa6\xa7\xa8\xa9')) / IPOption('\x00') / TCP() / "aaaaaaaaaaa"
# p.show()
hexdump(p)
sendp(p, iface = "veth0")
