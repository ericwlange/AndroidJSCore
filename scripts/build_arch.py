#!/usr/bin/python

import os,sys
from subprocess import call, check_output
import config_common

PLATFORMS = {
    'armeabi'     : 'arch-arm',
    'armeabi-v7a' : 'arch-arm',
    'arm64-v8a'   : 'arch-arm64',
    'x86'         : 'arch-x86',
    'x86_64'      : 'arch-x86_64',
    'mips'        : 'arch-mips',
    'mips64'      : 'arch-mips64'
}

HOSTS = {
    'armeabi'     : 'arm-eabi',
    'armeabi-v7a' : 'arm-eabiv7a',
    'arm64-v8a'   : 'arm-eabiv8a',
    'x86'         : 'x86',
    'x86_64'      : 'x86_64',
    'mips'        : 'mips',
    'mips64'      : 'mips64'
}

PREFIXES = {
    'armeabi'     : 'arm-linux-androideabi',
    'armeabi-v7a' : 'arm-linux-androideabi',
    'arm64-v8a'   : 'aarch64-linux-android',
    'x86'         : 'i686-linux-android',
    'x86_64'      : 'x86_64-linux-android',
    'mips'        : 'mipsel-linux-android',
    'mips64'      : 'mips64el-linux-android'
}

MARCH = {
    'armeabi'     : '-march=armv7',
    'armeabi-v7a' : '-march=armv7-a',
    'arm64-v8a'   : '',
    'x86'         : '-march=i686 -D__clang_minor__=3 -Wno-macro-redefined',
    'x86_64'      : '',
    'mips'        : '',
    'mips64'      : ''
}

def build_glib(abi):
    if os.path.exists(abi+'/third_party/lib/libglib-2.0.a'):
        return 0

    os.environ['SRC_ROOT']   = os.path.abspath(config_common.SOURCE)
    os.environ['CONFIG_DIR'] = os.path.abspath(config_common.CONFIG)
    os.environ['PREBUILT']   = os.path.abspath(abi+'/'+config_common.TOOLCHAIN)
    os.environ['ARCH']       = PLATFORMS[abi]
    os.environ['ABI']        = abi
    os.environ['INSTALL_LOC']= os.path.abspath(abi) + '/third_party'
    os.environ['PREFIX']     = PREFIXES[abi]
    os.environ['HOST']       = HOSTS[abi]

    output = call(['sh', '../scripts/build_glib.sh'])
    if output != 0:
        print "ERROR: Can't build GLIB library for " + abi
    return output

def build_icu(abi):
    if os.path.exists(abi+'/third_party/lib/libicuuc.a'):
        return 0

    os.environ['SRC_ROOT']   = os.path.abspath(config_common.SOURCE)
    os.environ['PREBUILT']   = os.path.abspath(abi+'/'+config_common.TOOLCHAIN)
    os.environ['ABI']        = abi
    os.environ['INSTALL_LOC']= os.path.abspath(abi) + '/third_party'
    os.environ['PREFIX']     = PREFIXES[abi]
    os.environ['ARCH']       = PLATFORMS[abi]
    os.environ['MARCH']      = MARCH[abi]

    output = call(['sh', '../scripts/build_icu.sh'])
    if output != 0:
        print "ERROR: Can't build GLIB library for " + abi
    return output

def main(argv):
    for abi in argv:
        output = build_glib(abi)
        if output!=0: return output
        
        output = build_icu(abi)
        if output!=0: return output

if __name__ == "__main__":
   main(sys.argv[1:])