DESCRIPTION = "Userspace framebuffer boot logo based on usplash."
HOMEPAGE = "http://svn.o-hand.com/view/misc/trunk/psplash/"
SECTION = "base"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://psplash.h;md5=a87c39812c1e37f3451567cc29a29c8f"

PV = "0.0+svnr${SRCREV}"
PR = "r5"

SRC_URI = "svn://svn.o-hand.com/repos/misc/trunk;module=psplash;proto=http \
           file://psplash-init"

S = "${WORKDIR}/psplash"

inherit autotools pkgconfig update-rc.d

FILES_${PN} += "/mnt/.psplash"

do_install_prepend() {
	install -d ${D}/mnt/.psplash/
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/psplash-init ${D}${sysconfdir}/init.d/psplash.sh
}

INITSCRIPT_NAME = "psplash.sh"
INITSCRIPT_PARAMS = "start 0 S . stop 20 0 1 6 ."