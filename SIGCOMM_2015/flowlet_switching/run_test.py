#!/usr/bin/env python

# Copyright 2013-present Barefoot Networks, Inc. 
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import time

NUM_PACKETS = 500

import random

import threading
from scapy.all import sniff
from scapy.all import Ether, IP, IPv6
from scapy.all import sendp

class PacketQueue:
    def __init__(self):
        self.pkts = []
        self.lock = threading.Lock()
        self.ifaces = set()

    def add_iface(self, iface):
        self.ifaces.add(iface)

    def get(self):
        self.lock.acquire()
        if not self.pkts:
            self.lock.release()
            return None, None
        pkt = self.pkts.pop(0)
        self.lock.release()
        return pkt

    def add(self, iface, pkt):
        if iface not in self.ifaces:
            return
        self.lock.acquire()
        self.pkts.append( (iface, pkt) )
        self.lock.release()

queue = PacketQueue()

def pkt_handler(pkt, iface):
    if IPv6 in pkt:
        return
    queue.add(iface, pkt)

class SnifferThread(threading.Thread):
    def __init__(self, iface, handler = pkt_handler):
        threading.Thread.__init__(self)
        self.iface = iface
        self.handler = handler

    def run(self):
        sniff(
            iface = self.iface,
            prn = lambda x: self.handler(x, self.iface)
        )

class PacketDelay:
    def __init__(self, bsize, bdelay, imin, imax, num_pkts = 100):
        self.bsize = bsize
        self.bdelay = bdelay
        self.imin = imin
        self.imax = imax
        self.num_pkts = num_pkts
        self.current = 1

    def __iter__(self):
        return self

    def next(self):
        if self.num_pkts <= 0:
            raise StopIteration
        self.num_pkts -= 1
        if self.current == self.bsize:
            self.current = 1
            return random.randint(self.imin, self.imax)
        else:
            self.current += 1
            return self.bdelay


pkt = Ether()/IP(dst='10.0.0.1', ttl=64)

port_map = {
    1: "veth3",
    2: "veth5",
    3: "veth7"
}

iface_map = {}
for p, i in port_map.items():
    iface_map[i] = p

queue.add_iface("veth3")
queue.add_iface("veth5")

for p, iface in port_map.items():
    t = SnifferThread(iface)
    t.daemon = True
    t.start()

import socket

send_socket = socket.socket(socket.AF_PACKET, socket.SOCK_RAW,
                            socket.htons(0x03))
send_socket.bind((port_map[3], 0))

delays = PacketDelay(10, 5, 25, 100, NUM_PACKETS)
ports = []
print "Sending", NUM_PACKETS, "packets ..."
for d in delays:
    # sendp is too slow...
    # sendp(pkt, iface=port_map[3], verbose=0)
    send_socket.send(str(pkt))
    time.sleep(d / 1000.)
time.sleep(1)
iface, pkt = queue.get()
while pkt:
    ports.append(iface_map[iface])
    iface, pkt = queue.get()
print ports
