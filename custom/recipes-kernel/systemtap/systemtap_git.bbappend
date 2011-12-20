FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}"

LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

inherit gettext module-base
DEPENDS = "elfutils sqlite3 systemtap-native virtual/kernel"

SRCREV = "f1d4f4313f6aa3e46211a47cfdcccfbd70bf769e"

PR = "r7"
PV = "1.6+git${SRCPV}"

SRC_URI = " \
	git://sourceware.org/git/systemtap.git;protocol=git \
	"
# systemtap doesn't support mips
COMPATIBLE_HOST = '(x86_64.*|i.86.*|powerpc.*|arm.*)-linux'

TARGET_CFLAGS += "-Wno-error=unused-parameter"

# Compile and install the uprobes kernel module.  Note that staprun
# expects it in the systemtap/runtime directory, not in /lib/modules.
do_compile_append() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS CC LD CPP
	oe_runmake CC="${KERNEL_CC}" LD="${KERNEL_LD}" AR="${KERNEL_AR}" \
		   -C ${STAGING_KERNEL_DIR} scripts
	oe_runmake KDIR=${STAGING_KERNEL_DIR}   \
		   M="${S}/runtime/uprobes/" \
		   CC="${KERNEL_CC}" LD="${KERNEL_LD}" \
		   AR="${KERNEL_AR}" \
		   -C "${S}/runtime/uprobes/"
}

do_install_append() {
	install -d ${D}/usr/share/systemtap/runtime/uprobes/
	install -m 0644 ${S}/runtime/uprobes/uprobes.ko ${D}/usr/share/systemtap/runtime/uprobes/
}

FILES_${PN} += "/usr/share/systemtap/runtime/uprobes"
