# ex:ts=4:sw=4:sts=4:et
# -*- tab-width: 4; c-basic-offset: 4; indent-tabs-mode: nil -*-
#
# BitBake Build System Python Library
#
# Copyright (C) 2003  Holger Schurig
# Copyright (C) 2003, 2004  Chris Larson
#
# Based on Gentoo's portage.py.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

__version__ = "1.13.3"

import sys
if sys.version_info < (2, 6, 0):
    raise RuntimeError("Sorry, python 2.6.0 or later is required for this version of bitbake")

import os
import logging


class NullHandler(logging.Handler):
    def emit(self, record):
        pass

Logger = logging.getLoggerClass()
class BBLogger(Logger):
    def __init__(self, name):
        if name.split(".")[0] == "BitBake":
            self.debug = self.bbdebug
        Logger.__init__(self, name)

    def bbdebug(self, level, msg, *args, **kwargs):
        return self.log(logging.DEBUG - level + 1, msg, *args, **kwargs)

    def plain(self, msg, *args, **kwargs):
        return self.log(logging.INFO + 1, msg, *args, **kwargs)

    def verbose(self, msg, *args, **kwargs):
        return self.log(logging.INFO - 1, msg, *args, **kwargs)

logging.raiseExceptions = False
logging.setLoggerClass(BBLogger)

logger = logging.getLogger("BitBake")
logger.addHandler(NullHandler())
logger.setLevel(logging.INFO)

# This has to be imported after the setLoggerClass, as the import of bb.msg
# can result in construction of the various loggers.
import bb.msg

if "BBDEBUG" in os.environ:
    level = int(os.environ["BBDEBUG"])
    if level:
        bb.msg.set_debug_level(level)

if True or os.environ.get("BBFETCH2"):
    from bb import fetch2 as fetch
    sys.modules['bb.fetch'] = sys.modules['bb.fetch2']

# Messaging convenience functions
def plain(*args):
    logger.plain(''.join(args))

def debug(lvl, *args):
    if isinstance(lvl, basestring):
        logger.warn("Passed invalid debug level '%s' to bb.debug", lvl)
        args = (lvl,) + args
        lvl = 1
    logger.debug(lvl, ''.join(args))

def note(*args):
    logger.info(''.join(args))

def warn(*args):
    logger.warn(''.join(args))

def error(*args):
    logger.error(''.join(args))

def fatal(*args):
    logger.critical(''.join(args))
    sys.exit(1)


def deprecated(func, name=None, advice=""):
    """This is a decorator which can be used to mark functions
    as deprecated. It will result in a warning being emmitted
    when the function is used."""
    import warnings

    if advice:
        advice = ": %s" % advice
    if name is None:
        name = func.__name__

    def newFunc(*args, **kwargs):
        warnings.warn("Call to deprecated function %s%s." % (name,
                                                             advice),
                      category=DeprecationWarning,
                      stacklevel=2)
        return func(*args, **kwargs)
    newFunc.__name__ = func.__name__
    newFunc.__doc__ = func.__doc__
    newFunc.__dict__.update(func.__dict__)
    return newFunc

# For compatibility
def deprecate_import(current, modulename, fromlist, renames = None):
    """Import objects from one module into another, wrapping them with a DeprecationWarning"""
    import sys

    module = __import__(modulename, fromlist = fromlist)
    for position, objname in enumerate(fromlist):
        obj = getattr(module, objname)
        newobj = deprecated(obj, "{0}.{1}".format(current, objname),
                            "Please use {0}.{1} instead".format(modulename, objname))
        if renames:
            newname = renames[position]
        else:
            newname = objname

        setattr(sys.modules[current], newname, newobj)

deprecate_import(__name__, "bb.fetch", ("MalformedUrl", "encodeurl", "decodeurl"))
deprecate_import(__name__, "bb.utils", ("mkdirhier", "movefile", "copyfile", "which"))
deprecate_import(__name__, "bb.utils", ["vercmp_string"], ["vercmp"])
