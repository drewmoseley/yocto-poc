DESCRIPTION = "SystemTap - script-directed dynamic tracing and performance analysis tool for Linux"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS = "elfutils sqlite3"

SRCREV = "f52d32a9f57d228627ee08e39f0bbcf3f3faae20"
PR = "r2"
PV = "1.6+git${SRCPV}"

SRC_URI = " \
	git://sources.redhat.com/git/systemtap.git;protocol=git \
        "

BUILD_CFLAGS += "-Wno-error=unused-parameter"

EXTRA_OECONF += "--with-libelf=${STAGING_DIR_TARGET} --without-rpm \
	     ac_cv_file__usr_include_nss=no \
	     ac_cv_file__usr_include_nss3=no \
	     ac_cv_file__usr_include_nspr=no \
	     ac_cv_file__usr_include_nspr4=no \
	     ac_cv_file__usr_include_avahi_client=no \
	     ac_cv_file__usr_include_avahi_common=no "

SRC_URI[md5sum]    = "cb202866ed704c44a876d041f788bdee"
SRC_URI[sha256sum] = "8ffe35caec0d937bd23fd78a3a8d94b58907cc0de0330b35e38f9f764815c459"

# systemtap doesn't support mips
COMPATIBLE_HOST = '(x86_64.*|i.86.*|powerpc.*|arm.*)-linux'

S = "${WORKDIR}/git"

inherit autotools gettext native

FILES_${PN}-dbg += "${libexecdir}/systemtap/.debug"
