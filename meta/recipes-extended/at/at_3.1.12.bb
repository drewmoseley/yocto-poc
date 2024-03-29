SUMMARY = "Delayed job execution and batch processing"
DESCRIPTION = "At allows for commands to be run at a particular time.  Batch will execute commands when \
the system load levels drop to a particular level."
SECTION = "base"
LICENSE="GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=4325afd396febcb659c36b49533135d4"
DEPENDS = "flex libpam initscripts \
	${@base_contains('DISTRO_FEATURES', 'pam', 'libpam', '', d)}"
RCONFLICTS_${PN} = "atd"
RREPLACES_${PN} = "atd"
PR = "r5"

SRC_URI = "${DEBIAN_MIRROR}/main/a/at/at_${PV}.orig.tar.gz \
    file://configure.patch \
    file://use-ldflags.patch \
    file://nonstripbinaries.patch \
    file://fix_parallel_build_error.patch \
    file://posixtm.c \
    file://posixtm.h \
    file://file_replacement_with_gplv2.patch \
    file://S99at \
    ${@base_contains('DISTRO_FEATURES', 'pam', '${PAM_SRC_URI}', '', d)}"

PAM_SRC_URI = "file://pam.conf.patch \
               file://configure-add-enable-pam.patch"

SRC_URI[md5sum] = "1e67991776148fb319fd77a2e599a765"
SRC_URI[sha256sum] = "7c55c6ab4fbe8add9e68f31b2b0ebf3fe805c9a4e7cfb2623a3d8a4789cc18f3"

EXTRA_OECONF += "ac_cv_path_SENDMAIL=/bin/true \
                 --with-daemon_username=root \
                 --with-daemon_groupname=root \
                 --with-jobdir=/var/spool/at/jobs \
                 --with-atspool=/var/spool/at/spool \
                 ${@base_contains('DISTRO_FEATURES', 'pam', '--with-pam', '--without-pam', d)} "

inherit autotools

do_compile_prepend () {
	cp -f ${WORKDIR}/posixtm.[ch] ${S}
}

do_install () {
	oe_runmake "IROOT=${D}" install

	install -d ${D}${sysconfdir}/init.d
	install -d ${D}${sysconfdir}/rcS.d
	install -m 0755    ${WORKDIR}/S99at		${D}${sysconfdir}/init.d/atd
	ln -sf		../init.d/atd		${D}${sysconfdir}/rcS.d/S99at

	for feature in ${DISTRO_FEATURES}; do
		if [ "$feature" = "pam" ]; then
			install -D -m 0644 ${WORKDIR}/${P}/pam.conf ${D}${sysconfdir}/pam.d/atd
			break
		fi
	done
}

pkg_postinst_${PN} () {
	if [ "x$D" != "x" ] ; then
		exit 1
	fi

	# below is necessary to allow at usable to normal users
	# now at is has its own /var/spool/at instead of under /var/spool/cron
	# this way is better to allow setgid on both sides
	grep "^daemon" /etc/group || groupadd daemon
	chown root:daemon /usr/bin/at
	chmod 2755 /usr/bin/at

	chown root:daemon -R /var/spool/at
	chmod 770 -R /var/spool/at

	chown root:daemon /etc/at.deny
	chmod 640 /etc/at.deny
}

PARALLEL_MAKE = ""
