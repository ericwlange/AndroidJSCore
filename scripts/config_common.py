import os,sys
from subprocess import call

CONFIG       = '../config/'
THIRD_PARTY  = CONFIG + 'third_party.txt'
PATCHES      = '../patches/'
CACHE        = 'cache/'
SOURCE       = 'Source/'
TOOLCHAIN    = 'toolchain/'

repos = []

PKG_DIR  = 0
PKG_URL  = 1
PKG_FNAME= 2

DEFAULT_ABIS = ['armeabi', 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64', 'mips', 'mips64']

ANDROID_ABIS = os.environ.get('ANDROID_ABIS')
if ANDROID_ABIS == None:
    ANDROID_ABIS = DEFAULT_ABIS
else:
    ANDROID_ABIS = ANDROID_ABIS.rsplit()

REQUIRED_ENV = ['ANDROID_SDK_ROOT', 'ANDROID_NDK_ROOT']

def check_env_vars():
    ret = 0
    for env in REQUIRED_ENV:
        if os.environ.get(env) == None:
            print 'ERROR: Environment variable ' + env + ' not set.'
            ret = -1
    return ret

def read_third_party():
    third_party = []
    if os.path.exists(THIRD_PARTY):
        with open(THIRD_PARTY) as f:
            for line in f:
                third_party.append(line.rsplit())
        i = 0
        for pkg in third_party:
            i = i + 1
            if len(pkg) == 0:
                continue
            if not len(pkg) == 2:
                print 'ERROR: line ' + str(i) + ' of ' + THIRD_PARTY + ' is in the wrong format.'
                return -1
            fname = os.path.basename(pkg[PKG_URL])
            if fname == 'master.zip':
                fname = pkg[PKG_DIR] + '.zip'
            repos.append((pkg[PKG_DIR],pkg[PKG_URL],CACHE + fname))
        return 0
    else:
        return -1

def apply_patches():
    output = call(['cp -r ' + PATCHES+'*' + ' ' + SOURCE],shell=True)
    return output
