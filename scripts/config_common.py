import os,sys
from subprocess import call, check_output
import inspect

def require(tool, env):
    path = os.environ.get(env) or tool
    path = check_output(['which',path])
    if path is None or path.strip() == '':
        print "'" + tool + "' is required.  Either put in PATH or set " + env
        return None
    return path.strip()

UNAME = require('uname', 'UNAME')
CP    = require('cp',    'CP')
TOUCH = require('touch', 'TOUCH')
MAKE  = require('make',  'MAKE')
if not UNAME or not CP or not TOUCH or not MAKE:
   sys.exit(-1)

SRC_ROOT     = os.path.dirname(inspect.getfile(inspect.currentframe()))

CONFIG       = SRC_ROOT + '/../config/'
THIRD_PARTY  = CONFIG + 'third_party.txt'
PATCHES      = SRC_ROOT + '/../patches/'
SCRIPTS      = SRC_ROOT
CACHE        = 'cache/'
SOURCE       = 'Source/'

def determine_host():
	output = check_output([UNAME,'-a'])
	if "Darwin" in output: return 'MacOSX'
	else: return 'Linux/gcc'

ANDROID_NDK_ROOT = os.environ.get('ANDROID_NDK_ROOT')
HOST             = determine_host()

repos = []

PKG_DIR  = 0
PKG_URL  = 1
PKG_FNAME= 2

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
