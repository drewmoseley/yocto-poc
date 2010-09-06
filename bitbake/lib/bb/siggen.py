import hashlib
import re

try:
    import cPickle as pickle
except ImportError:
    import pickle
    bb.msg.note(1, bb.msg.domain.Cache, "Importing cPickle failed. Falling back to a very slow implementation.")

def init(d):
    siggens = [obj for obj in globals().itervalues()
                      if type(obj) is type and issubclass(obj, SignatureGenerator)]

    desired = bb.data.getVar("BB_SIGNATURE_HANDLER", d, True) or "noop"
    for sg in siggens:
        if desired == sg.name:
            return sg(d)
            break
    else:
        bb.error("Invalid signature generator '%s', using default 'noop' generator" % desired)
        bb.error("Available generators: %s" % ", ".join(obj.name for obj in siggens))
        return SignatureGenerator(d)

class SignatureGenerator(object):
    """
    """
    name = "noop"

    def __init__(self, data):
        return

    def finalise(self, fn, d):
        return

class SignatureGeneratorBasic(SignatureGenerator):
    """
    """
    name = "basic"

    def __init__(self, data):
        self.basehash = {}
        self.taskhash = {}
        self.taskdeps = {}
        self.runtaskdeps = {}
        self.gendeps = {}
        self.lookupcache = {}
        self.basewhitelist = (data.getVar("BB_HASHBASE_WHITELIST", True) or "").split()
        self.taskwhitelist = data.getVar("BB_HASHTASK_WHITELIST", True) or None

        if self.taskwhitelist:
            self.twl = re.compile(self.taskwhitelist)
        else:
            self.twl = None

    def _build_data(self, fn, d):

        self.taskdeps[fn], self.gendeps[fn] = bb.data.generate_dependencies(d)

        basehash = {}
        lookupcache = {}

        for task in self.taskdeps[fn]:
            data = d.getVar(task, False)
            lookupcache[task] = data
            for dep in sorted(self.taskdeps[fn][task]):
                if dep in self.basewhitelist:
                    continue
                if dep in lookupcache:
                    var = lookupcache[dep]
                else:
                    var = d.getVar(dep, False)
                    lookupcache[dep] = var
                if var:
                    data = data + var
            self.basehash[fn + "." + task] = hashlib.md5(data).hexdigest()
            #bb.note("Hash for %s is %s" % (task, tashhash[task]))

        self.lookupcache[fn] = lookupcache

    def finalise(self, fn, d, variant):

        if variant:
            fn = "virtual:" + variant + ":" + fn

        self._build_data(fn, d)

        #Slow but can be useful for debugging mismatched basehashes
        #for task in self.taskdeps[fn]:
        #    self.dump_sigtask(fn, task, d.getVar("STAMP", True), False)

        for task in self.taskdeps[fn]:
            d.setVar("BB_BASEHASH_task-%s" % task, self.basehash[fn + "." + task])

    def get_taskhash(self, fn, task, deps, dataCache):
        k = fn + "." + task
        data = dataCache.basetaskhash[k]
        self.runtaskdeps[k] = deps
        for dep in sorted(deps):
            if self.twl and self.twl.search(dataCache.pkg_fn[fn]):
                #bb.note("Skipping %s" % dep)
                continue
            if dep not in self.taskhash:
                 bb.fatal("%s is not in taskhash, caller isn't calling in dependency order?", dep)
            data = data + self.taskhash[dep]
        h = hashlib.md5(data).hexdigest()
        self.taskhash[k] = h
        #d.setVar("BB_TASKHASH_task-%s" % task, taskhash[task])
        return h

    def dump_sigtask(self, fn, task, stampbase, runtime):
        k = fn + "." + task
        if runtime:
            sigfile = stampbase + "." + task + ".sigdata" + "." + self.taskhash[k]
        else:
            sigfile = stampbase + "." + task + ".sigbasedata" + "." + self.basehash[k]
        data = {}
        data['basewhitelist'] = self.basewhitelist
        data['taskdeps'] = self.taskdeps[fn][task]
        data['basehash'] = self.basehash[k]
        data['gendeps'] = {}
        data['varvals'] = {}
        data['varvals'][task] = self.lookupcache[fn][task]
        for dep in self.taskdeps[fn][task]:
            data['gendeps'][dep] = self.gendeps[fn][dep]
            data['varvals'][dep] = self.lookupcache[fn][dep]

        if runtime:
            data['runtaskdeps'] = self.runtaskdeps[k]
            data['runtaskhashes'] = {}
            for dep in data['runtaskdeps']:
                data['runtaskhashes'][dep] = self.taskhash[dep]

        p = pickle.Pickler(file(sigfile, "wb"), -1)
        p.dump(data)

    def dump_sigs(self, dataCache):
        for fn in self.taskdeps:
            for task in self.taskdeps[fn]:
                k = fn + "." + task
                if k not in self.taskhash:
                    continue
                if dataCache.basetaskhash[k] != self.basehash[k]:
                    bb.error("Bitbake's cached basehash does not match the one we just generated!")
                    bb.error("The mismatched hashes were %s and %s" % (dataCache.basetaskhash[k], self.basehash[k]))
                self.dump_sigtask(fn, task, dataCache.stamp[fn], True)

def compare_sigfiles(a, b):
    p1 = pickle.Unpickler(file(a, "rb"))
    a_data = p1.load()
    p2 = pickle.Unpickler(file(b, "rb"))
    b_data = p2.load()

    #print "Checking"
    #print str(a_data)
    #print str(b_data)

    def dict_diff(a, b):
        sa = set(a.keys())
        sb = set(b.keys())
        common = sa & sb
        changed = set()
        for i in common:
            if a[i] != b[i]:
                changed.add(i)
        added = sa - sb
        removed = sb - sa
        return changed, added, removed 

    if a_data['basewhitelist'] != b_data['basewhitelist']:
        print "basewhitelist changed from %s to %s" % (a_data['basewhitelist'], b_data['basewhitelist'])

    if a_data['taskwhitelist'] != b_data['taskwhitelist']:
        print "taskwhitelist changed from %s to %s" % (a_data['taskwhitelist'], b_data['taskwhitelist'])


    if a_data['taskdeps'] != b_data['taskdeps']:
        print "Task dependencies changed from %s to %s" % (sorted(a_data['taskdeps']), sorted(b_data['taskdeps']))

    if a_data['basehash'] != b_data['basehash']:
        print "basehash changed from %s to %s" % (a_data['basehash'], b_data['basehash'])

    changed, added, removed = dict_diff(a_data['gendeps'], b_data['gendeps'])
    if changed:
        for dep in changed:
            print "List of dependencies for variable %s changed from %s to %s" % (dep, a_data['gendeps'][dep], b_data['gendeps'][dep])
    #if added:
    #    print "Dependency on variable %s was added (value %s)" % (dep, b_data['gendeps'][dep])
    #if removed:
    #    print "Dependency on Variable %s was removed (value %s)" % (dep, a_data['gendeps'][dep])


    changed, added, removed = dict_diff(a_data['varvals'], b_data['varvals'])
    if changed:
        for dep in changed:
            print "Variable %s value changed from %s to %s" % (dep, a_data['varvals'][dep], b_data['varvals'][dep])
    #if added:
    #    print "Dependency on variable %s was added (value %s)" % (dep, b_data['gendeps'][dep])
    #if removed:
    #    print "Dependency on Variable %s was removed (value %s)" % (dep, a_data['gendeps'][dep])

    if 'runtaskdeps' in a_data and 'runtaskdeps' in b_data and a_data['runtaskdeps'] != b_data['runtaskdeps']:
        print "Tasks this task depends on changed from %s to %s" % (a_data['taskdeps'], b_data['taskdeps'])

    if 'runtaskhashes' in a_data:
        for dep in a_data['runtaskhashes']:
            if a_data['runtaskhashes'][dep] != b_data['runtaskhashes'][dep]:
                print "Hash for dependent task %s changed from %s to %s" % (dep, a_data['runtaskhashes'][dep], b_data['runtaskhashes'][dep])





