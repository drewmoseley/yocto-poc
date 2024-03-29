INHIBIT_DEFAULT_DEPS = "1"
LICENSE = "GPLv2 & LGPLv2.1"

BPN = "eglibc"

do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

# Binary locales are generated at build time if ENABLE_BINARY_LOCALE_GENERATION
# is set. The idea is to avoid running localedef on the target (at first boot)
# to decrease initial boot time and avoid localedef being killed by the OOM
# killer which used to effectively break i18n on machines with < 128MB RAM.

# default to disabled 
ENABLE_BINARY_LOCALE_GENERATION ?= "0"
ENABLE_BINARY_LOCALE_GENERATION_pn-eglibc-locale-nativesdk = "0"

#enable locale generation on these arches
# BINARY_LOCALE_ARCHES is a space separated list of regular expressions
BINARY_LOCALE_ARCHES ?= "arm.* i[3-6]86 x86_64 powerpc mips"

# set "1" to use cross-localedef for locale generation
# set "0" for qemu emulation of native localedef for locale generation
LOCALE_GENERATION_WITH_CROSS-LOCALEDEF = "1"

PR = "r16"

PKGSUFFIX = ""
PKGSUFFIX_virtclass-nativesdk = "-nativesdk"

PROVIDES = "virtual/libc-locale${PKGSUFFIX}"

PACKAGES = "localedef${PKGSUFFIX} ${PN}-dbg"

PACKAGES_DYNAMIC = "locale-base-* \
                    eglibc-gconv-* eglibc-charmap-* eglibc-localedata-* eglibc-binary-localedata-* \
                    glibc-gconv-*${PKGSUFFIX}  glibc-charmap-*  glibc-localedata-*  glibc-binary-localedata-*"

DESCRIPTION_localedef = "eglibc: compile locale definition files"

# eglibc-gconv is dynamically added into PACKAGES, thus
# FILES_eglibc-gconv will not be automatically extended in multilib.
# Explicitly add ${MLPREFIX} for FILES_eglibc-gconv.
FILES_${MLPREFIX}eglibc-gconv = "${libdir}/gconv/*"
FILES_${PN}-dbg += "${libdir}/gconv/.debug/*"
FILES_localedef${PKGSUFFIX} = "${bindir}/localedef"

LOCALETREESRC = "${STAGING_INCDIR}/eglibc-locale-internal-${MULTIMACH_TARGET_SYS}"

do_install () {
	mkdir -p ${D}${bindir} ${D}${datadir} ${D}${libdir}
	cp -fpPR ${LOCALETREESRC}/${bindir}/* ${D}${bindir}
	cp -fpPR ${LOCALETREESRC}/${libdir}/locale ${D}${libdir}
	cp -fpPR ${LOCALETREESRC}/${libdir}/gconv ${D}${libdir}
	cp -fpPR ${LOCALETREESRC}/${datadir}/i18n ${D}${datadir}
	cp -fpPR ${LOCALETREESRC}/${datadir}/locale ${D}${datadir}
	cp -fpPR ${LOCALETREESRC}/SUPPORTED ${WORKDIR}
}

inherit libc-package

do_install[depends] += "virtual/libc${PKGSUFFIX}:do_populate_sysroot"

BBCLASSEXTEND = "nativesdk"
