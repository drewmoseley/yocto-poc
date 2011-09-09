#!/bin/bash                                                                     
# usage: crosstap <systemtap script> <user@target-addr>                         
#                                                                               
# NOTE: SYSTEMTAP_HOST_INSTALLDIR, SYSTEMTAP_TARGET_ARCH,                       
# STAGING_BINDIR_TOOLCHAIN, STAGING_BINDIR_TOOLPREFIX, and                      
# TARGET_KERNEL_BUILDDIR must be set to the appropriate host/yocto              
# build-tree paths for this script to work correctly.  The values               
# below are only examples.                                                      

YOCTO_DIR="/home/aarshad/yocto-m3/yocto-poc"

# where systemtap was installed on the host                                     
SYSTEMTAP_HOST_INSTALLDIR="$YOCTO_DIR/build/tmp/sysroots/i686-linux/usr"

# i386 for 32-bit x86, arm for arm                                              
SYSTEMTAP_TARGET_ARCH="arm"

# where to find compiler executables, passed on to kernel kbuild                
STAGING_BINDIR_TOOLCHAIN="$YOCTO_DIR/build/tmp/sysroots/i686-linux/usr/bin/armv7a-vfp-neon-poky-linux-gnueabi"
STAGING_BINDIR_TOOLPREFIX="arm-poky-linux-gnueabi"

# pointer to configured/built kernel tree                                       
TARGET_KERNEL_BUILDDIR="$YOCTO_DIR/build/tmp/work/beagleboard-poky-linux-gnueabi/linux-yocto-2.6.37+git1+84f1a422d7e21fbc23a687035bdf9d42471f19e0_1+3ddb22772862a8223640fa97580569924f51bddc-r20/linux-beagleboard-standard-build"

script_name=$(basename "$1")
script_base=${script_name%.*}

${SYSTEMTAP_HOST_INSTALLDIR}/bin/stap -gv -a ${SYSTEMTAP_TARGET_ARCH} -B CROSS_COMPILE="${STAGING_BINDIR_TOOLCHAIN}/${STAGING_BINDIR_TOOLPREFIX}-" -r ${TARGET_KERNEL_BUILDDIR} -I ${SYSTEMTAP_HOST_INSTALLDIR}/share/systemtap/tapset -R ${SYSTEMTAP_HOST_INSTALLDIR}/share/systemtap/runtime -m $script_base $1

echo "copying stap module $script_base.ko"
scp $script_base.ko $2:$script_base.ko

echo "executing stap module $script_base.ko"
ssh -t $2 staprun $script_base.ko

echo "removing stap module $script_base.ko"
ssh -t $2 rm $script_base.ko

