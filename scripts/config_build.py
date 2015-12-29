#!/usr/bin/python

import os,sys
from subprocess import call
import config_common
from config_common import HOST

HOST_ICU         = 'icu_host/'

WGET  = config_common.require('wget', 'WGET')
RM    = config_common.require('rm',   'RM')
SH    = config_common.require('sh',   'SH')
UNZIP = config_common.require('unzip','UNZIP')
TAR   = config_common.require('tar',  'TAR')
PATCH = config_common.require('patch','PATCH')
if not WGET or not RM or not SH or not UNZIP or not TAR or not PATCH:
   sys.exit(-1)

def setup_dirs():
    dirs = [config_common.CACHE, config_common.SOURCE]

    for dir in dirs:
        if not os.path.exists(dir):
            os.makedirs(dir)
    return 0

def get_third_party():
    for pkg in config_common.repos:
        if not os.path.exists(pkg[config_common.PKG_FNAME]):
            output = call([WGET,'-O',pkg[config_common.PKG_FNAME],pkg[config_common.PKG_URL]])
            if output != 0:
                call([RM,'-f',pkg[config_common.PKG_FNAME]])
                print 'ERROR: ' + pkg[config_common.PKG_URL] + ' failed to download.'
                return output
            call([RM,'-rf',config_common.SOURCE+pkg[config_common.PKG_DIR]]);
        if not os.path.exists(config_common.SOURCE+pkg[config_common.PKG_DIR]):
            if pkg[config_common.PKG_FNAME].endswith('.zip'):
                output = call([UNZIP,pkg[config_common.PKG_FNAME],'-d',config_common.SOURCE])
            elif pkg[config_common.PKG_FNAME].endswith('tar.gz') or pkg[config_common.PKG_FNAME].endswith('.tgz'):
                output = call([TAR,'zxvf',pkg[config_common.PKG_FNAME],'-C',config_common.SOURCE])
            elif pkg[config_common.PKG_FNAME].endswith('tar.xz') or pkg[config_common.PKG_FNAME].endswith('.tgx'):
                output = call([TAR,'xvf',pkg[config_common.PKG_FNAME],'-C',config_common.SOURCE])
            else: output = -1
            if output==0 and os.path.exists(config_common.PATCHES+pkg[config_common.PKG_DIR]+'.patch'):
                output = call([PATCH,
                    '-d',  config_common.SOURCE+pkg[config_common.PKG_DIR],
                    '-p1',
                    '-i',  config_common.PATCHES+pkg[config_common.PKG_DIR]+'.patch' ])
            if output != 0:
                call([RM,'-f',pkg[config_common.PKG_DIR]])
                print 'ERROR: ' + pkg[config_common.PKG_FNAME] + ' failed to uncompress/patch'
                return output
    return 0

def build_icu_host():
    if not os.path.exists(HOST_ICU):
        os.makedirs(HOST_ICU)
    else:
        return 0
    
    os.environ['HOST'] = HOST
    output = call([SH, config_common.SCRIPTS + '/build_icu_host.sh'])
    if output != 0:
        print "ERROR: Can't build host ICU."
        call([RM,'-rf',HOST_ICU])
    return output
    
def main(argv):
    result = config_common.check_env_vars()
    if result != 0: return result

    result = setup_dirs()
    if result != 0: return result

    result = config_common.read_third_party()
    if result != 0: return result

    result = get_third_party()
    if result != 0: return result

    result = build_icu_host()
    if result != 0: return result
    
    return 0

if __name__ == "__main__":
   main(sys.argv[1:])