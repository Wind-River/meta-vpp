DESCRIPTION = "Runtime dependencies for VPP"

# LICENSE = "MIT"
# LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PACKAGE_ARCH = "${MACHINE_ARCH}"
inherit packagegroup
PACKAGES = "\
	${PN} \
	"

RDEPENDS_${PN} = " \
		dpdk \
		numactl \
		openssh \
		openssl \
		coreutils \
		util-linux \
		procps \
		console-tools \
		iputils \
		iproute2 \
		numactl \
		tunctl \
		ethtool \
		pciutils \
		vpp-core-plugin-acl \
		vpp-core \
		"
