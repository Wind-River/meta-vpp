meta-vpp
=========

Introduction
------------------------

This  layer  is intended to enable FD.io/technology Vector Packet
Processing (FD.io/technology and FD.io/view/VPP) on poky.


Dependencies
-------------------------

This layer depends on:

```
	URI: git://git.yoctoproject.org/meta-dpdk
	revision: HEAD

	URI: git://git.yoctoproject.org/meta-intel
	revision: HEAD

	URI: git://git.openembedded.org/meta-openembedded/
	revision: HEAD

	layes: meta-oe
	meta-python
	meta-networking
```
You are solely responsible for determining the appropriateness of using or redistributing the above dependencies and assume any risks associated with your exercise of permissions under the license.

Maintenance
-------------------------

Maintainer:
        Babak Sarashki  <babak.sarashki@windriver.com>


Building the meta-vpp layer
---------------------------

In order to enable FD.io VPP on a poky target,  add  this   layer
and  its dependencies to conf/bblayers.conf. Append packagegroup-
vpp to IMAGE_INSTALL  and  set  MACHINE  =  "intel-corei7-64"  in
conf/local.conf


Use Case: Virtual Network Setup
-------------------------------

This script is inteded to setup virtual network between ns's on the host.

```
#!/bin/bash

map() {
	
cat << EOF
Source: https://wiki.fd.io/view/VPP/Tutorial_Routing_and_Switching
Setting up Virtual network Demo

          2001:1::ffff/64   2001::ffff/64
          10.0.10.1/24      10.0.0.10/24

          +----------+      +---------------+      +----------------+      +--------------+
          |  Vpp Tap |------|  Vpp Routing  |------| Loopback intrf |------| VPP brdg Dmn |
          +----------+      +---------------+      +----------------+      +--------------+
               |                                                                   |
               |                                                                   |
               |                                                                   |
               |                                                                   |
               |                                           +-----------------------+
               |                                           |                       |
               |                                           |                       |
               |                                           |                       |
               |                                    +----------------+      +----------------+
               |                                    | Host Interface |      | Host Interface |  
               |                                    |    host-vpp1   |      |    host-vpp0   |  
               |                                    +----------------+      +----------------+
               |                                           |                       |
               |                                           |                       |
               |                                           |                       |
         +-------------+                            +----------------+       +--------------+
         |  Tap 0      |                            |      Vpp1      |       |    Vpp0      | 
         |2001:1::1/64 |                            |  2001::2/64    |       | 2001::1/64   | 
         |10.0.1.1/24  |                            |  10.0.0.2/24   |       | 10.0.0.1/24  |
         | ns2         |                            |    ns1 vethns1 |       | ns0 vethns0  |
         +-------------+                            +----------------+       +--------------+

EOF

}

setup() {
		# Add network namespace ns0
		ip netns add ns0
		ip netns add ns1
		ip netns add ns2

		# Add Virtual Ethernet devs
		ip link add vpp0 type veth peer name vethns0
		ip link add vpp1 type veth peer name vethns1


		# Create tap device
		vppctl tap connect ns2-tap0
		vppctl set interface state tapcli-0 up

		# Set tap and Veths to their netns
		ip link set vethns0 netns ns0
		ip link set vethns1 netns ns1
		ip link set ns2-tap0 netns ns2

		# Bring up lo devs
		ip netns exec ns0 ip link set lo up
		ip netns exec ns1 ip link set lo up
		ip netns exec ns2 ip link set lo up

		vppctl create loopback interface
		vppctl set interface l2 bridge loop0 1 bvi
		vppctl set interface state loop0 up

		# Create host interrace
		vppctl create host-interface name vpp0
		vppctl create host-interface name vpp1

		# Bridge the host interfaces
		vppctl set interface l2 bridge host-vpp0 1
		vppctl set interface l2 bridge host-vpp1 1

		# Bring up the bridge
		vppctl set interface state host-vpp0 up
		vppctl set interface state host-vpp1 up


		# Bring up vpp0 and vpp1
		ip link set vpp0 up
		ip link set vpp1 up

		# vppctl show int

		# Add addr
		ip netns exec ns0 ip addr add 2001::1/64 dev vethns0
		ip netns exec ns0 ip addr add 10.0.0.1/24 dev vethns0
		ip netns exec ns0 ip link set vethns0 up
		ip netns exec ns0 ethtool -K vethns0 rx off tx off >/dev/null


		ip netns exec ns1 ip addr add 2001::2/64 dev vethns1
		ip netns exec ns1 ip addr add 10.0.0.2/24 dev vethns1
		ip netns exec ns1 ip link set vethns1 up
		ip netns exec ns1 ethtool -K vethns1 rx off tx off > /dev/null


		ip netns exec ns2 ip link set lo up
		ip netns exec ns2 ip link set ns2-tap0 up
		ip netns exec ns2 ip addr add 10.0.1.1/24 dev ns2-tap0
		ip netns exec ns2 ip addr add 2001:1::1/64 dev ns2-tap0


		vppctl set interface ip addr tapcli-0 2001:1::ffff/64
		vppctl set interface ip addr tapcli-0 10.0.1.10/24


		vppctl set interface ip address loop0 2001::ffff/64
		vppctl set interface ip address loop0 10.0.0.10/24

		ip netns exec ns0 ip route add default via 10.0.0.10
		ip netns exec ns0 ip -6 route add default via 2001::ffff
		ip netns exec ns1 ip route add default via 10.0.0.10
		ip netns exec ns1 ip -6 route add default via 2001::ffff
		ip netns exec ns2 ip route add default via 10.0.1.10
		ip netns exec ns2 ip -6 route add default via 2001:1::ffff
} 

cleanup () {
	pkill -9 vpp
	vpp -c /etc/vpp/startup.conf
	ip netns del ns0
	ip netns del ns1
	ip netns del ns2
}

if [ $# -ne 1 ]; then
        echo "usage: $0 <setup|cleanup|map>"
        exit
fi
$1
```

# Legal Notices

All product names, logos, and brands are property of their respective owners. All company, product and service names used in this software are for identification purposes only. Wind River is a registered trademarks of Wind River Systems, Inc. Linux is a registered trademark of Linus Torvalds.

Disclaimer of Warranty / No Support: Wind River does not provide support and maintenance services for this software, under Wind River’s standard Software Support and Maintenance Agreement or otherwise. Unless required by applicable law, Wind River provides the software (and each contributor provides its contribution) on an “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, either express or implied, including, without limitation, any warranties of TITLE, NONINFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness of using or redistributing the software and assume any risks associated with your exercise of permissions under the license.
