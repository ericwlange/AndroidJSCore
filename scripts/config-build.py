import os,sys
from subprocess import call
import config_common

TOOLCHAIN        = 'toolchain/'
ANDROID_NDK_ROOT = os.environ.get('ANDROID_NDK_ROOT')
GCC_VERSION      = '4.9'
SYSTEM           = 'linux-x86_64'
HOST_ICU         = 'icu_host/'
HOST             = 'Linux/gcc' # or 'MacOSX'

TOOLCHAINS = {
    'armeabi'     : 'arm-linux-androideabi-',
    'armeabi-v7a' : 'arm-linux-androideabi-',
    'arm64-v8a'   : 'aarch64-linux-android-',
    'x86'         : 'x86-',
    'x86_64'      : 'x86_64-',
    'mips'        : 'mipsel-linux-android-',
    'mips64'      : 'mips64el-linux-android-'
}

def setup_dirs():
    dirs = [config_common.CACHE, config_common.SOURCE]
    dirs.extend(config_common.ANDROID_ABIS)

    for dir in dirs:
        if not os.path.exists(dir):
            os.makedirs(dir)
    return 0

def get_third_party():
    for pkg in config_common.repos:
        if not os.path.exists(pkg[config_common.PKG_FNAME]):
            output = call(['wget','-O',pkg[config_common.PKG_FNAME],pkg[config_common.PKG_URL]])
            if output != 0:
                print 'ERROR: ' + pkg[config_common.PKG_URL] + ' failed to download.'
                return output
            if pkg[config_common.PKG_FNAME].endswith('.zip'):
                output = call(['unzip',pkg[config_common.PKG_FNAME],'-d',config_common.SOURCE])
            elif pkg[config_common.PKG_FNAME].endswith('tar.gz') or fname.endswith('.tgz'):
                output = call(['tar','zxvf',pkg[config_common.PKG_FNAME],'-C',config_common.SOURCE])
            elif pkg[config_common.PKG_FNAME].endswith('tar.xz') or fname.endswith('.tgx'):
                output = call(['tar','xvf',pkg[config_common.PKG_FNAME],'-C',config_common.SOURCE])
            else: output = -1
            if output != 0:
                call(['rm','-f',pkg[config_common.PKG_FNAME]])
                print 'ERROR: ' + pkg[config_common.PKG_FNAME] + ' failed to uncompress.'
                return output
    return 0

def create_toolchain(abi):
    if os.path.exists(abi+'/'+TOOLCHAIN):
        return 0
    output = call([
        'bash',
        ANDROID_NDK_ROOT + '/build/tools/make-standalone-toolchain.sh',
        '--toolchain='+TOOLCHAINS[abi]+GCC_VERSION,
        '--platform=android-21',
        '--install-dir='+abi+'/'+TOOLCHAIN,
        '--system='+SYSTEM,
        '--stl=gnustl'
    ])
    if output != 0:
        print "ERROR: Can't build toolchain for " + abi
        call(['rm','-rf',abi+'/'+TOOLCHAIN])
    return output

def build_icu_host():
    if not os.path.exists(HOST_ICU):
        os.makedirs(HOST_ICU)
    else:
        return 0
    
    os.environ['HOST'] = HOST
    output = call(['sh', '../scripts/build_icu_host.sh'])
    if output != 0:
        print "ERROR: Can't build host ICU."
        call(['rm','-rf',HOST_ICU])
    return output

result = config_common.check_env_vars()
if result != 0: sys.exit(result)

result = setup_dirs()
if result != 0: sys.exit(result)

result = config_common.read_third_party()
if result != 0: sys.exit(result)

result = get_third_party()
if result != 0: sys.exit(result)

for toolchain in config_common.ANDROID_ABIS:
    result = create_toolchain(toolchain)
    if result != 0: sys.exit(result)

result = build_icu_host()
if result != 0: sys.exit(result)

