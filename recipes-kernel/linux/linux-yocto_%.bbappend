FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

# Enable tun device support in the kernel
SRC_URI += "file://vpp-kernel.cfg"
