#
# Configuration to use external CSL 2011.03-71 toolchain
#

EXTERNAL_TOOLCHAIN ?= "/usr/local/csl/2011.03"
TOOLCHAIN_PATH ?= "${EXTERNAL_TOOLCHAIN}"

TARGET_VENDOR = "-none"
CSTRIPLET_powerpc = "powerpc-linux-gnu"
CSTRIPLET_powerpc64 = "powerpc-linux-gnu"
CSTRIPLET_arm = "arm-none-linux-gnueabi"
CSTRIPLET_i686 = "i686-pc-linux-gnu"
CSTRIPLET = "${TARGET_SYS}"
TARGET_PREFIX = "${CSTRIPLET}-"
TOOLCHAIN_SYSPATH ?= "${TOOLCHAIN_PATH}/${CSTRIPLET}"

PATH =. "${EXTERNAL_TOOLCHAIN}/bin:"

PREFERRED_PROVIDER_linux-libc-headers ?= "external-toolchain-csl"
PREFERRED_PROVIDER_libgcc = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}compilerlibs = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-initial = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}gcc-intermediate = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}g++ = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}binutils = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-for-gcc = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/${TARGET_PREFIX}libc-initial = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/libc = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/libc-locale = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/libintl = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/libiconv = "external-toolchain-csl"
PREFERRED_PROVIDER_glibc-thread-db = "external-toolchain-csl"
PREFERRED_PROVIDER_virtual/linux-libc-headers = "external-toolchain-csl"
PREFERRED_PROVIDER_gdbserver = "external-toolchain-csl"
PREFERRED_PROVIDER_binutils-cross = "external-toolchain-csl"

TARGET_CPPFLAGS_prepend = " -isystem${TOOLCHAIN_SYSPATH}/include "
TARGET_LDFLAGS_prepend = " -L${TOOLCHAIN_SYSPATH}/lib -Wl,-rpath-link,${TOOLCHAIN_SYSPATH}/lib "

TARGET_CC_ARCH_append_arm = " -mno-unaligned-access"
TARGET_CC_KERNEL_ARCH_append_arm = " -mno-unaligned-access"

TOOLCHAIN_OPTIONS = " --sysroot=${STAGING_DIR_HOST}"

OECMAKE_EXTRA_ROOT_PATH += "${TOOLCHAIN_PATH}"

