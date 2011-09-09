PR = "r13"

INHIBIT_DEFAULT_DEPS = "1"

INSANE_SKIP_libgcc = "True"
INSANE_SKIP_libstdc++ = "True"
INSANE_SKIP_gdbserver = "True"

PACKAGE_STRIP = "no"

SRC_URI = "file://SUPPORTED"

PROVIDES = "\
	virtual/${TARGET_PREFIX}gcc \
	virtual/${TARGET_PREFIX}g++ \
	virtual/${TARGET_PREFIX}gcc-initial \
	virtual/${TARGET_PREFIX}gcc-intermediate \
	virtual/${TARGET_PREFIX}binutils \
	virtual/${TARGET_PREFIX}compilerlibs \
	libgcc \
	binutils-cross \
	virtual/${TARGET_PREFIX}libc-for-gcc \
	virtual/${TARGET_PREFIX}libc-initial \
	virtual/libc \
	virtual/libintl \
	virtual/libiconv \
	glibc-thread-db \
	${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', 'external-toolchain-csl', 'linux-libc-headers', '', d)} \
	${@base_conditional('PREFERRED_PROVIDER_gdbserver', 'external-toolchain-csl', 'gdbserver', '', d)} \
"

DEPENDS = "${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', 'external-toolchain-csl', '', 'linux-libc-headers', d)}"
RPROVIDES_glibc-dev += "libc-dev virtual-libc-dev"
PACKAGES_DYNAMIC += "glibc-gconv-*"
PACKAGES_DYNAMIC += "glibc-locale-*"
PACKAGES_DYNAMIC += "locale-base-*"

LEAD_SONAME = "libc.so.6"

PACKAGES = "\
	libgcc \
	libgcc-dev \
	libstdc++ \
	libstdc++-dev \
	${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', 'external-toolchain-csl', 'linux-libc-headers', '', d)} \
	${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', 'external-toolchain-csl', 'linux-libc-headers-dev', '', d)} \
	${@base_conditional('PREFERRED_PROVIDER_gdbserver', 'external-toolchain-csl', 'gdbserver', '', d)} \
	glibc-dbg \
	glibc \
	catchsegv \
	sln \
	nscd \
	ldd \
	localedef \
	glibc-dev \
	glibc-utils \
	glibc-pic \
	glibc-locale \
	libsegfault \
	glibc-extra-nss \
	glibc-thread-db \
	glibc-pcprofile \
	binutils-symlinks \
"

ALLOW_EMPTY_binutils-symlinks = "1"
FILES_binutils-symlinks = ""

FILES_glibc = "\
	${sysconfdir} \
	${libexecdir}/* \
	${datadir}/zoneinfo \
	/lib/libc* \
	/lib/libm* \
	/lib/ld* \
	/lib/libpthread* \
	/lib/libresolv* \
	/lib/librt* \
	/lib/libutil* \
	/lib/libnsl* \
	/lib/libnss_files* \
	/lib/libnss_compat* \
	/lib/libnss_dns* \
	/lib/libdl* \
	/lib/libanl* \
	/lib/libBrokenLocale* \
	/sbin/ldconfig \
"

FILES_glibc-dev = "\
	${includedir} \
	${libdir}/*.o \
	${bindir}/rpcgen \
	${libdir}/*.so \
	${libdir}/*.a \
"

FILES_glibc-dbg += " ${bindir}/.debug ${base_sbindir}/.debug ${sbindir}/.debug ${libdir}/.debug ${libdir}/gconv/.debug ${libexecdir}/*/.debug ${base_libdir}/.debug ${libdir}/.debug"
FILES_glibc-utils = "${bindir}/* ${sbindir}/*"
FILES_glibc-pic = "${libdir}/*_pic.a ${libdir}/*_pic.map ${libdir}/libc_pic/"
FILES_glibc-extra-nss = "/lib/libnss*"
FILES_glibc-gconv = "${libdir}/gconv/*"
FILES_glibc-pcprofile = "/lib/libpcprofile.so"
FILES_glibc-thread-db = "/lib/libthread_db*"

FILES_libgcc = "${base_libdir}/libgcc_s.so.1"
FILES_libgcc-dev = "${base_libdir}/libgcc_s.so"

FILES_libstdc++ = "${libdir}/libstdc++.so.*"
FILES_libstdc++-dev = "\
	${includedir}/c++/* \
	${libdir}/libstdc++.so \
	${libdir}/libstdc++.la \
	${libdir}/libstdc++.a \
	${libdir}/libsupc++.la \
	${libdir}/libsupc++.a \
"

FILES_linux-libc-headers = "\
	${includedir}/asm* \
	${includedir}/linux \
	${includedir}/mtd \
	${includedir}/rdma \
	${includedir}/scsi \
	${includedir}/sound \
	${includedir}/video \
"

FILES_libsegfault = "/lib/libSegFault*"
FILES_catchsegv = "${bindir}/catchsegv"
RDEPENDS_catchsegv = "libsegfault"

FILES_ldd = "${bindir}/ldd"
FILES_nscd = "${sbindir}/nscd*"
FILES_sln = "${base_sbindir}/sln"
FILES_localedef = "${bindir}/localedef"

FILES_gdbserver = "${bindir}/gdbserver ${libdir}/bin/sysroot-gdbserver"

DESCRIPTION_glibc-utils = "glibc: misc utilities like iconf, local, gencat, tzselect, rpcinfo, ..."
DESCRIPTION_glibc-extra-nss = "glibc: nis, nisplus and hesiod search services"
DESCRIPTION_ldd = "glibc: print shared library dependencies"
DESCRIPTION_nscd = "glibc: name service cache daemon for passwd, group, and hosts"
DESCRIPTION_sln = "glibc: create symbolic links between files"
DESCRIPTION_localedef = "glibc: compile locale definition files"
DESCRIPTION_gdbserver = "gdb - GNU debugger"

def popen(cmd, **kwargs):
	import oe.process
	import subprocess

	if 'stderr' not in kwargs:
		kwargs['stderr'] = subprocess.PIPE

	return oe.process.Popen(cmd, **kwargs)

def csl_run(d, cmd, *args):
	topdir = d.getVar('TOPDIR', True)
	toolchain_path = d.getVar('TOOLCHAIN_PATH', True)
	target_prefix = d.getVar('TARGET_PREFIX', True)
	path = os.path.join(toolchain_path, 'bin', target_prefix + cmd)
	args = [path] + list(args)
	try:
		pipe = popen(args, cwd=topdir)
	except OSError:
		return None, None
	else:
		stdout, stderr = pipe.communicate()
		return stdout, stderr

def csl_get_version(d):
	stdout, stderr = csl_run(d, 'gcc', '-v')
	if stdout is None:
		bb.error('Failed to obtain CodeSourcery toolchain version')
		return 'UNKNOWN'
	last_line = stderr.splitlines()[-1]
	return last_line

def csl_get_main_version(d):
	version = csl_get_version(d)
	if version != 'UNKNOWN':
		return version.split()[-1].rstrip(')')
	else:
		return version

def csl_get_gcc_version(d):
	version = csl_get_version(d)
	if version != 'UNKNOWN':
		return version.split()[2]
	else:
		return version

def csl_get_libc_version(d):
	import os,bb
	if os.path.exists(bb.data.getVar('TOOLCHAIN_SYSPATH', d, 1)+'/libc/lib/'):
		for file in os.listdir(bb.data.getVar('TOOLCHAIN_SYSPATH', d, 1)+'/libc/lib/'):
			if file.find('libc-') == 0:
				return file[5:-3]
		return None

def csl_get_kernel_version(d):
	import os,bb
	if os.path.exists(bb.data.getVar('TOOLCHAIN_SYSPATH', d, 1)+'/libc/'):
		f = open(bb.data.getVar('TOOLCHAIN_SYSPATH', d, 1)+'/libc/usr/include/linux/version.h', 'r')
		l = f.readlines();
		f.close();
		for s in l:
			if s.find('LINUX_VERSION_CODE') > 0:
				ver = int(s.split()[2])
				maj = ver / 65536
				ver = ver % 65536
				min = ver / 256
				ver = ver % 256
				return str(maj)+'.'+str(min)+'.'+str(ver)
		return None

def csl_get_gdb_version(d):
	stdout, stderr = csl_run(d, 'gdb', '-v')
	if stdout is None:
		return 'UNKNOWN'
	first_line = stdout.splitlines()[0]
	return first_line.split()[-1]

def target_core(d):
	return {'armv7a-vfp-neon':'armv7-a-neon', 'ppce500mc':'te500mc', 'ppce500':'te500v1', 'ppce500v2':'te500v2', 'ppce600':'te600'}.get(bb.data.getVar('TUNE_PKGARCH',d,1), '')

CSL_VER_MAIN := "${@csl_get_main_version(d)}"
CSL_VER_GCC := "${@csl_get_gcc_version(d)}"
CSL_VER_LIBC := "${@csl_get_libc_version(d)}"
CSL_VER_KERNEL := "${@csl_get_kernel_version(d)}"
CSL_VER_GDBSERVER := "${@csl_get_gdb_version(d)}"
CSL_LIC_RLE := "${@["GPLv3 with GCC RLE", "GPLv2 with GCC RLE"][csl_get_main_version(d) <= "2009q1-203"]}"
CSL_LIC_GDBSERVER := "${@["GPLv2+", "GPLv3+"][csl_get_gdb_version(d) >= "6.7.1"]}"

PKGV = "${CSL_VER_MAIN}"
PKGV_libgcc = "${CSL_VER_GCC}"
PKGV_libgcc-dev = "${CSL_VER_GCC}"
PKGV_libstdc++ = "${CSL_VER_GCC}"
PKGV_libstdc++-dev = "${CSL_VER_GCC}"
PKGV_libc = "${CSL_VER_LIBC}"
PKGV_glibc = "${CSL_VER_LIBC}"
PKGV_glibc-dev = "${CSL_VER_LIBC}"
PKGV_glibc-dbg = "${CSL_VER_LIBC}"
PKGV_glibc-utils = "${CSL_VER_LIBC}"
PKGV_glibc-gconv = "${CSL_VER_LIBC}"
PKGV_glibc-extra-nss = "${CSL_VER_LIBC}"
PKGV_glibc-thread-db = "${CSL_VER_LIBC}"
PKGV_glibc-pcprofile = "${CSL_VER_LIBC}"
PKGV_catchsegv = "${CSL_VER_LIBC}"
PKGV_sln = "${CSL_VER_LIBC}"
PKGV_nscd = "${CSL_VER_LIBC}"
PKGV_ldd = "${CSL_VER_LIBC}"
PKGV_localedef = "${CSL_VER_LIBC}"
PKGV_libsegfault = "${CSL_VER_LIBC}"
PKGV_linux-libc-headers = "${CSL_VER_KERNEL}"
PKGV_gdbserver = "${CSL_VER_GDBSERVER}"

# shut up LIC_FILES_CHECKSUM
LICENSE = "CLOSED"

LICENSE_ldd = "LGPLv2.1+"
LICENSE_glibc = "LGPLv2.1+"
LICENSE_glibc-thread-db = "LGPLv2.1+"
LICENSE_libgcc = "${CSL_LIC_RLE}"
LICENSE_libgcc-dev = "${CSL_LIC_RLE}"
LICENSE_libstdc++ = "${CSL_LIC_RLE}"
LICENSE_libstdc++-dev = "${CSL_LIC_RLE}"
LICENSE_gdbserver = "${CSL_LIC_GDBSERVER}"

AUTO_LIBNAME_PKGS_pn-external-toolchain-csl = ""
DEBIAN_NAMES = ""
CORE = "${@target_core(d)}"

do_install() {
	${@base_contains('DEBUG_FLAGS', '-g0', '', '[ ! -f ${TOOLCHAIN_SYSPATH}/libc/${CORE}/lib/.debug/ld-${CSL_VER_LIBC}.so.debug ] && echo "You need to install the debug info add-on via Sourcery CodeBench.  Please refer to the trouble-shooting section of the Mentor Embedded Linux Use\'s Manual." && exit 1',d)}
	install -d ${D}${sysconfdir}
	install -d ${D}${bindir}
	install -d ${D}${includedir}
	install -d ${D}${sbindir} ${D}${sbindir}/.debug
	install -d ${D}${base_bindir} ${D}${base_bindir}/.debug
	install -d ${D}${libdir}
	install -d ${D}${base_libdir}
	install -d ${D}${base_sbindir} ${D}${base_sbindir}/.debug
	install -d ${D}${datadir}

	cp -a ${TOOLCHAIN_SYSPATH}/libc/${CORE}/lib/* ${D}${base_libdir}
	${@base_contains('DEBUG_FLAGS', '-g0', '', 'cp -a ${TOOLCHAIN_SYSPATH}/libc/${CORE}/lib/.debug ${D}${base_libdir}',d)}
	cp -a ${TOOLCHAIN_SYSPATH}/libc/${CORE}/etc/* ${D}${sysconfdir}
	cp -a ${TOOLCHAIN_SYSPATH}/libc/${CORE}/usr/* ${D}/usr
	mv ${D}/usr/lib/bin/nscd* ${D}${sbindir}
	${@base_contains('DEBUG_FLAGS', '-g0', '', 'mv ${D}/usr/lib/bin/.debug/nscd* ${D}${sbindir}/.debug/',d)}
	mv ${D}/usr/lib/bin/sln* ${D}${base_sbindir}
	${@base_contains('DEBUG_FLAGS', '-g0', '', 'mv ${D}/usr/lib/bin/.debug/sln* ${D}${base_sbindir}/.debug/',d)}
	mv ${D}/usr/lib/bin/* ${D}${bindir}
	${@base_contains('DEBUG_FLAGS', '-g0', '', 'mv ${D}/usr/lib/bin/.debug/* ${D}${bindir}/.debug/',d)}
	cp -a ${TOOLCHAIN_SYSPATH}/include/* ${D}${includedir}
	cp -a ${TOOLCHAIN_SYSPATH}/libc/usr/include/* ${D}${includedir}
	rm -rf ${D}${libdir}/locale/*
	${@base_conditional('PREFERRED_PROVIDER_linux-libc-headers', 'external-toolchain-csl', '', 'rm -rf ${D}/usr/include/linux', d)}
	${@base_conditional('PREFERRED_PROVIDER_gdbserver', 'external-toolchain-csl', 'ln -s ${bindir}/gdbserver ${D}${libdir}/bin/sysroot-gdbserver', 'rm -rf ${D}${bindir}/gdbserver', d)}
	rm -rf ${D}${sysconfdir}/rpc
	rm -rf ${D}${datadir}/zoneinfo

	sed -e "s# /lib# ../../lib#g" -e "s# /usr/lib# .#g" ${D}${libdir}/libc.so > ${D}${libdir}/temp
	mv ${D}${libdir}/temp ${D}${libdir}/libc.so

	sed -e "s# /lib# ../../lib#" -e "s# /usr/lib# .#g" ${D}${libdir}/libpthread.so > ${D}${libdir}/temp
	mv ${D}${libdir}/temp ${D}${libdir}/libpthread.so
}

SYSROOT_PREPROCESS_FUNCS += "add_core_symlink"
add_core_symlink() {
	if [ -n "${CORE}" ]; then
		ln -s . ${SYSROOT_DESTDIR}/${CORE}
	fi
}

TMP_LOCALE="/tmp/locale${libdir}/locale"

locale_base_postinst() {
#!/bin/sh

if [ "x$D" != "x" ]; then
	exit 1
fi

rm -rf ${TMP_LOCALE}
mkdir -p ${TMP_LOCALE}
if [ -f ${libdir}/locale/locale-archive ]; then
	cp ${libdir}/locale/locale-archive ${TMP_LOCALE}/
fi
localedef --inputfile=${datadir}/i18n/locales/%s --charmap=%s --prefix=/tmp/locale %s
mkdir -p ${libdir}/locale/
mv ${TMP_LOCALE}/locale-archive ${libdir}/locale/
rm -rf ${TMP_LOCALE}
}

locale_base_postrm() {
#!/bin/sh

rm -rf ${TMP_LOCALE}
mkdir -p ${TMP_LOCALE}
if [ -f ${libdir}/locale/locale-archive ]; then
	cp ${libdir}/locale/locale-archive ${TMP_LOCALE}/
fi
localedef --delete-from-archive --inputfile=${datadir}/locales/%s --charmap=%s --prefix=/tmp/locale %s
mv ${TMP_LOCALE}/locale-archive ${libdir}/locale/
rm -rf ${TMP_LOCALE}
}

python package_do_split_gconvs () {
	import os, re
	if (bb.data.getVar('PACKAGE_NO_GCONV', d, 1) == '1'):
		bb.note("package requested not splitting gconvs")
		return

	if not bb.data.getVar('PACKAGES', d, 1):
		return

	libdir = bb.data.getVar('libdir', d, 1)
	if not libdir:
		bb.error("libdir not defined")
		return
	datadir = bb.data.getVar('datadir', d, 1)
	if not datadir:
		bb.error("datadir not defined")
		return

	libcver = bb.data.getVar('CSL_VER_LIBC', d, 1)
	if not libcver:
		bb.error("CSL_VER_LIBC not defined")
		return

	gconv_libdir = os.path.join(libdir, "gconv")
	charmap_dir = os.path.join(datadir, "i18n", "charmaps")
	locales_dir = os.path.join(datadir, "i18n", "locales")
	binary_locales_dir = os.path.join(libdir, "locale")

	def set_pkg_ver(fn, pkg, file_regex, output_pattern, group):
		bb.data.setVar('PKGV_' + pkg, libcver, d)

	do_split_packages(d, gconv_libdir, file_regex='^(.*)\.so$', output_pattern='glibc-gconv-%s', description='gconv module for character set %s', hook=set_pkg_ver, extra_depends='glibc-gconv')

	do_split_packages(d, charmap_dir, file_regex='^(.*)\.gz$', output_pattern='glibc-charmap-%s', description='character map for %s encoding', hook=set_pkg_ver, extra_depends='')

	locale_section = bb.data.getVar('LOCALE_SECTION', d, True)
	def calc_locale_deps(fn, pkg, file_regex, output_pattern, group):
		deps = []
		f = open(fn, "r")
		c_re = re.compile('^copy "(.*)"')
		i_re = re.compile('^include "(\w+)".*')
		for l in f.readlines():
			m = c_re.match(l) or i_re.match(l)
			if m:
				dp = legitimize_package_name('glibc-localedata-%s' % m.group(1))
				if not dp in deps:
					deps.append(dp)
		f.close()
		if deps != []:
			bb.data.setVar('RDEPENDS_%s' % pkg, " ".join(deps), d)
		bb.data.setVar('PKGV_' + pkg, libcver, d)
		if locale_section:
			bb.data.setVar('SECTION_' + pkg, locale_section, d)

	do_split_packages(d, locales_dir, file_regex='(.*)', output_pattern='glibc-localedata-%s', description='locale definition for %s', hook=calc_locale_deps, extra_depends='')
	bb.data.setVar('PACKAGES', bb.data.getVar('PACKAGES', d) + ' glibc-gconv', d)

	f = open(os.path.join(bb.data.getVar('WORKDIR', d, 1), "SUPPORTED"), "r")
	supported = f.readlines()
	f.close()

	dot_re = re.compile("(.*)\.(.*)")

	# Collate the locales by base and encoding
	encodings = {}
	for l in supported:
		l = l[:-1]
		(locale, charset) = l.split(" ")
		m = dot_re.match(locale)
		if m:
			locale = m.group(1)
		if not encodings.has_key(locale):
			encodings[locale] = []
		encodings[locale].append(charset)

	def output_locale_source(name, locale, encoding):
		pkgname = 'locale-base-' + legitimize_package_name(name)

		bb.data.setVar('RDEPENDS_%s' % pkgname, 'localedef glibc-localedata-%s glibc-charmap-%s' % (legitimize_package_name(locale), legitimize_package_name(encoding)), d)
		rprovides = 'virtual-locale-%s' % legitimize_package_name(name)
		m = re.match("(.*)_(.*)", name)
		if m:
			rprovides += ' virtual-locale-%s' % m.group(1)
		bb.data.setVar('RPROVIDES_%s' % pkgname, rprovides, d)
		bb.data.setVar('PACKAGES', '%s %s' % (pkgname, bb.data.getVar('PACKAGES', d, 1)), d)
		bb.data.setVar('ALLOW_EMPTY_%s' % pkgname, '1', d)
		bb.data.setVar('pkg_postinst_%s' % pkgname, bb.data.getVar('locale_base_postinst', d, 1) % (locale, encoding, locale), d)
		bb.data.setVar('pkg_postrm_%s' % pkgname, bb.data.getVar('locale_base_postrm', d, 1) % (locale, encoding, locale), d)
		bb.data.setVar('PKGV_' + pkgname, libcver, d)

	def output_locale_binary(name, locale, encoding):
		target_arch = bb.data.getVar("TARGET_ARCH", d, 1)
		qemu = "qemu-%s" % target_arch
		pkgname = 'locale-base-' + legitimize_package_name(name)
		m = re.match("(.*)\.(.*)", name)
		if m:
			glibc_name = "%s.%s" % (m.group(1), m.group(2).lower().replace("-",""))
		else:
			glibc_name = name
		bb.data.setVar('RDEPENDS_%s' % pkgname, legitimize_package_name('glibc-binary-localedata-%s' % glibc_name), d)
		rprovides = 'virtual-locale-%s' % legitimize_package_name(name)
		m = re.match("(.*)_(.*)", name)
		if m:
			rprovides += ' virtual-locale-%s' % m.group(1)
		bb.data.setVar('RPROVIDES_%s' % pkgname, rprovides, d)
		bb.data.setVar('ALLOW_EMPTY_%s' % pkgname, '1', d)
		bb.data.setVar('PACKAGES', '%s %s' % (pkgname, bb.data.getVar('PACKAGES', d, 1)), d)
		bb.data.setVar('PKGV_' + pkgname, libcver, d)

		treedir = os.path.join(bb.data.getVar("WORKDIR", d, 1), "locale-tree")
		path = bb.data.getVar("PATH", d, 1)
		i18npath = os.path.join(treedir, datadir, "i18n")

		localedef_opts = "--force --old-style --no-archive --prefix=%s --inputfile=%s/i18n/locales/%s --charmap=%s %s" % (treedir, datadir, locale, encoding, name)
		cmd = "PATH=\"%s\" I18NPATH=\"%s\" %s -L %s %s/bin/localedef %s" % (path, i18npath, qemu, treedir, treedir, localedef_opts)
		bb.note("generating locale %s (%s)" % (locale, encoding))
		if os.system(cmd):
			raise bb.build.FuncFailed("localedef returned an error (command was %s)." % cmd)

	def output_locale(name, locale, encoding):
		output_locale_source(name, locale, encoding)

	# Reshuffle names so that UTF-8 is preferred over other encodings
	non_utf8 = []
	for l in encodings.keys():
		if len(encodings[l]) == 1:
			output_locale(l, l, encodings[l][0])
			if encodings[l][0] != "UTF-8":
				non_utf8.append(l)
		else:
			if "UTF-8" in encodings[l]:
				output_locale(l, l, "UTF-8")
				encodings[l].remove("UTF-8")
			else:
				non_utf8.append(l)
			for e in encodings[l]:
				output_locale('%s.%s' % (l, e), l, e)

	if non_utf8 != []:
		bb.note("the following locales are supported only in legacy encodings:")
		bb.note("  " + " ".join(non_utf8))
}

python package_do_split_locales() {
	if (bb.data.getVar('PACKAGE_NO_LOCALE', d, True) == '1'):
		bb.debug(1, "package requested not splitting locales")
		return

	packages = (bb.data.getVar('PACKAGES', d, True) or "").split()

	datadir = bb.data.getVar('datadir', d, True)

	pkgd = bb.data.getVar('PKGD', d, True)

	if not datadir:
		bb.note("datadir not defined")
		return

	if 'glibc-locale' in packages:
		packages.remove('glibc-locale')

	localedir = os.path.join(pkgd + datadir, 'locale')

	if not os.path.isdir(localedir):
		bb.debug(1, "No locale files in this package")
		return

	locales = os.listdir(localedir)
	for l in locales:
		ln = legitimize_package_name(l)
		pkg = 'glibc-locale-' + ln
		packages.append(pkg)
		bb.data.setVar('FILES_' + pkg, os.path.join(datadir, 'locale', l), d)
		bb.data.setVar('RDEPENDS_' + pkg, 'glibc virtual-locale-%s' % ln, d)
		bb.data.setVar('RPROVIDES_' + pkg, 'glibc-locale %s-translation' % ln, d)
		bb.data.setVar('DESCRIPTION_' + pkg, '%s translation for glibc' % l, d)
		bb.data.setVar('PKGV_' + pkg, bb.data.getVar('CSL_VER_LIBC', d, 1), d)

	bb.data.setVar('PACKAGES', ' '.join(packages), d)
}

python populate_packages_prepend () {
	if bb.data.getVar('DEBIAN_NAMES', d, 1):
		bb.data.setVar('PKG_glibc', 'libc6', d)
		bb.data.setVar('PKG_glibc-dev', 'libc6-dev', d)
		bb.data.setVar('PKG_libgcc', 'libgcc1', d)
		bb.data.setVar('PKG_libgcc-dev', 'libgcc1-dev', d)
	bb.build.exec_func('package_do_split_gconvs', d)
}

NATIVE_INSTALL_WORKS = "1"
