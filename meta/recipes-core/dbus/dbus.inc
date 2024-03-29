SUMMARY = "D-Bus message bus"
DESCRIPTION = "D-Bus is a message bus system, a simple way for applications to talk to one another. In addition to interprocess communication, D-Bus helps coordinate process lifecycle; it makes it simple and reliable to code a \"single instance\" application or daemon, and to launch applications and daemons on demand when their services are needed."
HOMEPAGE = "http://dbus.freedesktop.org"
SECTION = "base"
LICENSE = "AFL-2 | GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=10dded3b58148f3f1fd804b26354af3e \
                    file://dbus/dbus.h;firstline=6;endline=20;md5=6eea2e0c7750dd8e620dcb1437312fa5"
DEPENDS = "expat virtual/libintl virtual/libx11 libsm"
DEPENDS_virtclass-native = "expat-native virtual/libintl-native"
DEPENDS_virtclass-nativesdk = "expat-nativesdk virtual/libintl-nativesdk virtual/libx11"

SRC_URI = "http://dbus.freedesktop.org/releases/dbus/dbus-${PV}.tar.gz \
           file://tmpdir.patch; \
           file://dbus-1.init"

inherit autotools pkgconfig gettext update-rc.d

INITSCRIPT_NAME = "dbus-1"
INITSCRIPT_PARAMS = "start 02 5 3 2 . stop 20 0 1 6 ."

CONFFILES_${PN} = "${sysconfdir}/dbus-1/system.conf ${sysconfdir}/dbus-1/session.conf"

DEBIANNAME_${PN} = "dbus-1"

PACKAGES =+ "${PN}-lib ${PN}-x11 ${PN}-systemd"

FILES_${PN}-x11 = "${bindir}/dbus-launch"
RDEPENDS_${PN}-x11 = "${PN}"

FILES_${PN}-systemd = "${base_libdir}/systemd/system/"

FILES_${PN} = "${bindir}/dbus-daemon* \
               ${bindir}/dbus-uuidgen \
               ${bindir}/dbus-cleanup-sockets \
               ${bindir}/dbus-send \
               ${bindir}/dbus-monitor \
               ${libexecdir}/dbus* \
               ${sysconfdir} \
               ${datadir}/dbus-1/services \
               ${datadir}/dbus-1/system-services"
FILES_${PN}-lib = "${libdir}/lib*.so.*"
RRECOMMENDS_${PN}-lib = "${PN}"
FILES_${PN}-dev += "${libdir}/dbus-1.0/include ${bindir}/dbus-glib-tool"

pkg_postinst_dbus() {
	# can't do adduser stuff offline
	if [ "x$D" != "x" ]; then
		exit 1
	fi

	MESSAGEUSER=messagebus
	MESSAGEHOME=/var/run/dbus
	UUIDDIR=/var/lib/dbus

	mkdir -p $MESSAGEHOME
	mkdir -p $UUIDDIR
	chgrp "$MESSAGEUSER" "$MESSAGEHOME" 2>/dev/null || addgroup "$MESSAGEUSER"
	chown "$MESSAGEUSER":"$MESSAGEUSER" "$MESSAGEHOME" 2>/dev/null || \
		adduser --system --home "$MESSAGEHOME" --no-create-home --disabled-password \
			--ingroup "$MESSAGEUSER" "$MESSAGEUSER"

	chown "$MESSAGEUSER":"$MESSAGEUSER" "$UUIDDIR"

	grep -q netdev: /etc/group || addgroup netdev

	chown root:"$MESSAGEUSER" /usr/libexec/dbus-daemon-launch-helper
	chmod 4754 /usr/libexec/dbus-daemon-launch-helper

	# add volatile after new user/grp are created
	echo "d messagebus messagebus 0755 /var/run/dbus none" > /etc/default/volatiles/99_dbus
	if [ -e /etc/init.d/populate-volatile.sh ] ; then
		/etc/init.d/populate-volatile.sh update
	fi
}

EXTRA_OECONF_X = "--with-x"
EXTRA_OECONF_X_virtclass-native = "--without-x"

EXTRA_OECONF = "--disable-tests \
                --disable-checks \
                --disable-xml-docs \
                --disable-doxygen-docs \
                --disable-libaudit \
                --with-xml=expat \
                --with-systemdsystemunitdir=${base_libdir}/systemd/system/ \
                ${EXTRA_OECONF_X}"

do_install() {
	autotools_do_install

	install -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/dbus-1.init ${D}${sysconfdir}/init.d/dbus-1

	# disable dbus-1 sysv script on systemd installs
	# nearly all distros call the initscript plain 'dbus', but OE-core is different
	ln -sf /dev/null ${D}/${base_libdir}/systemd/system/dbus-1.service
}

do_install_virtclass-native() {
	autotools_do_install

	# for dbus-glib-native introspection generation
	install -d ${STAGING_DATADIR_NATIVE}/dbus/
	# N.B. is below install actually required?
	install -m 0644 bus/session.conf ${STAGING_DATADIR_NATIVE}/dbus/session.conf
	
	# dbus-glib-native and dbus-glib need this xml file
	./bus/dbus-daemon --introspect > ${STAGING_DATADIR_NATIVE}/dbus/dbus-bus-introspect.xml
}
BBCLASSEXTEND = "native nativesdk"
