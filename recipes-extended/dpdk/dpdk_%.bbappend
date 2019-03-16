
DPDK_CPU_CFLAGS  = "-pie -fPIC"
# DPDK_TARGET_MACHINE = "nhm"

do_compile_prepend() {
	export CPU_CFLAGS="${DPDK_CPU_CFLAGS}"
}

