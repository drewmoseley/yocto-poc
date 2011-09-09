
# Modifications:
# 1. Build systemtap-1.6 instead of systemtap-1.4 to get better support for ARM
# 2. Enable systemtap for ARM
# 3. Enable systemtap-native
# adeel_arshad@mentor.com

LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

inherit gettext
DEPENDS = "elfutils sqlite3 systemtap-native"
DEPENDS_virtclass-native = "elfutils sqlite3 gettext-native"
DEPENDS_virtclass-nativesdk = "elfutils sqlite3 gettext-native"

SRCREV = "820f2d22fc47fad6e09ba886efb9b91e1247cb39"

PR = "r5"
PV = "1.6+git${SRCPV}"

SRC_URI = "git://sourceware.org/git/systemtap.git;protocol=git \
          "
# systemtap doesn't work on arm and doesn't support mips
COMPATIBLE_HOST = '(x86_64.*|i.86.*|powerpc.*|arm.*)-linux'

BBCLASSEXTEND = "native nativesdk"
