
do_compile() {
	# apmd doesn't use whole autotools. Just libtool for installation
	oe_runmake "LIBTOOL=${STAGING_BINDIR_CROSS}/${HOST_SYS}-libtool" apm apmd
}

