DESCRIPTION = "Vector Packet Processing"

STABLE = "stable/1807"
BRANCH = "master"
SRCREV = "9f624cacf379c5349e66c12b91f0b4765f7ec22c"
S = "${WORKDIR}/git"
PV = "18.07"

LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

AUTOTOOLS_SCRIPT_PATH = "${S}/src"


SRC_URI = "git://github.com/FDio/vpp;branch=${STABLE} \
	file://0001-Link-vpp-api-with-shared-libs-if-static-is-disabled.patch \
	file://0001-GCC-above-5.4-fails-when-we-specify-arch-funattribut.patch \
	"
DEPENDS = "dpdk openssl numactl"

inherit autotools
inherit pkgconfig
inherit python-dir


EXTRA_OECONF = " \
	--disable-dependency-tracking \
	--with-log2-cache-line-bytes=6  \
	--srcdir=${S}/src \
	--enable-perftool \
	--disable-papi \
	--disable-japi \
	--disable-static \
	"

include vpp-pkgs.inc


do_configure_append () {
	( cd ${B} &&  mkdir -p vppinfra vpp/app )
}

do_install_append() {
	mkdir -p ${D}/etc/vpp
	cp ${S}/src/vpp/conf/startup.conf ${D}/etc/vpp/startup.conf
}

pkg_postinst_ontarget_${PN} () {
echo vm.nr_hugepages=1024 >> /etc/sysctl.conf

# Must be greater than or equal to (2 * vm.nr_hugepages).
echo  vm.max_map_count=3096 >> /etc/sysctl.conf

# All groups allowed to access hugepages
echo vm.hugetlb_shm_group=0 >> /etc/sysctl.conf

# Shared Memory Max must be greator or equal to the total size of hugepages.
# For 2MB pages, TotalHugepageSize = vm.nr_hugepages * 2 * 1024 * 1024
# If the existing kernel.shmmax setting  (cat /sys/proc/kernel/shmmax)
# is greater than the calculated TotalHugepageSize then set this parameter
# to current shmmax value.
echo kernel.shmmax=2147483648 >> /etc/sysctl.conf
	
# And add to rc.local
echo mkdir -p /var/log/vpp >> /etc/rc.local
echo "/usr/bin/vpp -c /etc/vpp/startup.conf" >> /etc/rc.local
chmod 755 /etc/rc.local
}
