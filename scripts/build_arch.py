#!/usr/bin/python

import os,sys
from subprocess import call, check_output
import config_common
from config_common import HOST, ANDROID_NDK_ROOT
import config_build

BASH   = config_common.require('bash',  'BASH')
RM     = config_common.require('rm',    'RM')
SH     = config_common.require('sh',    'SH')
CMAKE  = config_common.require('cmake', 'CMAKE')
MAKE   = config_common.require('make',  'MAKE')
PYTHON = config_common.require('python','PYTHON')
PERL   = config_common.require('perl',  'PERL')
RUBY   = config_common.require('ruby',  'RUBY')
BISON  = config_common.require('bison', 'BISON')
GPERF  = config_common.require('gperf', 'GPERF')

if not BASH or not RM or not SH or not CMAKE or not MAKE:
   sys.exit(-1)
if not PYTHON or not PERL or not RUBY or not BISON or not GPERF:
   sys.exit(-1)

DEFAULT_ABIS = ['armeabi', 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64', 'mips', 'mips64']

TOOLCHAIN    = 'toolchain/'

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

GCC_VERSION      = '4.9'
if HOST == 'MacOSX':
    SYSTEM_OS = 'darwin-x86_64'
else:
    SYSTEM_OS = 'linux-x86_64'

TOOLCHAINS_GCC = {
    'armeabi'     : 'arm-linux-androideabi-',
    'armeabi-v7a' : 'arm-linux-androideabi-',
    'arm64-v8a'   : 'aarch64-linux-android-',
    'x86'         : 'x86-',
    'x86_64'      : 'x86_64-',
    'mips'        : 'mipsel-linux-android-',
    'mips64'      : 'mips64el-linux-android-'
}

CLANG_VERSION  = '3.6'

def create_toolchain(abi):
    if os.path.exists(abi+'/'+TOOLCHAIN):
        return 0
    if not os.path.exists(abi):
        os.makedirs(abi)
    os.makedirs(abi+'/'+TOOLCHAIN)
    output = call([
		BASH,
		ANDROID_NDK_ROOT + '/build/tools/make-standalone-toolchain.sh',
		'--toolchain='+TOOLCHAINS_GCC[abi]+GCC_VERSION,
#        '--llvm-version='+CLANG_VERSION, # don't use CLANG -- too many bugs
		'--platform=android-21',
		'--install-dir='+abi+'/'+TOOLCHAIN,
		'--system='+SYSTEM_OS,
		'--stl=gnustl'
	])
    if output != 0:
        print "ERROR: Can't build toolchain for " + abi
        call([RM,'-rf',abi+'/'+TOOLCHAIN])
    return output
    
def set_env_variables(abi):
    os.environ['SRC_ROOT']   = os.path.abspath(config_common.SOURCE)
    os.environ['CONFIG_DIR'] = os.path.abspath(config_common.CONFIG)
    os.environ['PREBUILT']   = os.path.abspath(abi+'/'+TOOLCHAIN)
    os.environ['ARCH']       = PLATFORMS[abi]
    os.environ['ABI']        = abi
    os.environ['INSTALL_LOC']= os.path.abspath(abi) + '/third_party'
    os.environ['PREFIX']     = PREFIXES[abi]
    os.environ['HOST']       = HOSTS[abi]
    os.environ['CMAKE']      = CMAKE
    os.environ['MAKE']       = MAKE
    os.environ['PYTHON']     = PYTHON
    os.environ['PERL']       = PERL
    os.environ['RUBY']       = RUBY
    os.environ['BISON']      = BISON
    os.environ['GPERF']      = GPERF
    os.environ['JNI_LIBS']   = config_common.SRC_ROOT + '/../AndroidJSCore/AndroidJSCore/src/main/jniLibs'

def build_glib(abi):
    if os.path.exists(abi+'/third_party/lib/libglib-2.0.a'):
        return 0

    output = call([SH, config_common.SCRIPTS + '/build_glib.sh'])
    if output != 0:
        print "ERROR: Can't build GLIB library for " + abi
    return output

def build_icu(abi):
    if os.path.exists(abi+'/third_party/lib/libicuuc.a'):
        return 0

    output = call([SH, config_common.SCRIPTS + '/build_icu.sh'])
    if output != 0:
        print "ERROR: Can't build GLIB library for " + abi
    return output

def build_JavaScriptCore(abi):
    output = call([SH, config_common.SCRIPTS + '/build_JavaScriptCore.sh'])
    if output != 0:
        print "ERROR: Can't build JavaScriptCore library for " + abi
    return output

def main(argv):
    for abi in argv:
    	if abi not in DEFAULT_ABIS:
    	    print "ABI '" + abi + "' not supported."
    	    return -1
    	
        config_build.main([])    	
    	
    	set_env_variables(abi)
    	
    	output = create_toolchain(abi)
        if output!=0: return output
    
        output = build_glib(abi)
        if output!=0: return output
        
        output = build_icu(abi)
        if output!=0: return output

        output = build_JavaScriptCore(abi)
        if output!=0: return output

if __name__ == "__main__":
   main(sys.argv[1:])