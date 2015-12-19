#set PREBUILT
#set PREFIX
#set ABI
#set INSTALL_DIR
#set SRC_ROOT
#set ARCH
#set MARCH

set -e

export AR=${PREBUILT}/bin/${PREFIX}-ar
export ICU_SOURCES=$SRC_ROOT/icu
export ANDROIDVER=21
export API_LEVEL=21
export HOST_ICU=${PWD}/${ABI}/icu
export ICU_CROSS_BUILD=${PWD}/icu_host
export PLATFORM=$ANDROID_NDK_ROOT/platforms/android-$API_LEVEL/${ARCH}

mkdir -p $HOST_ICU
cd $HOST_ICU
export CFLAGS="--sysroot=${PLATFORM} -Os -DU_USING_ICU_NAMESPACE=1 -fno-short-enums \
-DUEXPORT2=__attribute__\(\(visibility\(\"default\"\)\)\) \
-DELF64_ST_INFO\(b\,t\)=\(\(\(b\)\<\<4\)+\(\(t\)\&0xf\)\) \
-DU_HAVE_NL_LANGINFO_CODESET=0 -D__STDC_INT64__ -DU_TIMEZONE=0 ${MARCH} \
-DUCONFIG_NO_LEGACY_CONVERSION=1 -DUCONFIG_NO_BREAK_ITERATION=1 \
-DUCONFIG_NO_COLLATION=0 -DUCONFIG_NO_FORMATTING=0 -DUCONFIG_NO_TRANSLITERATION=0 \
-DUCONFIG_NO_REGULAR_EXPRESSIONS=1 -D__STRICT_ANSI__=1"
export CXXFLAGS="--sysroot=${PLATFORM} -Os -DU_USING_ICU_NAMESPACE=1 -fno-short-enums \
-DUEXPORT2=__attribute__\(\(visibility\(\"default\"\)\)\) \
-DELF64_ST_INFO\(b\,t\)=\(\(\(b\)\<\<4\)+\(\(t\)\&0xf\)\) \
-DU_HAVE_NL_LANGINFO_CODESET=0 -D__STDC_INT64__ -DU_TIMEZONE=0 ${MARCH} \
-DUCONFIG_NO_LEGACY_CONVERSION=1 -DUCONFIG_NO_BREAK_ITERATION=1 \
-DUCONFIG_NO_COLLATION=0 -DUCONFIG_NO_FORMATTING=0 -DUCONFIG_NO_TRANSLITERATION=0 \
-DUCONFIG_NO_REGULAR_EXPRESSIONS=1 -D__STRICT_ANSI__=1"
export LDFLAGS="-lc -lstdc++ -Wl,-rpath-link=${PLATFORM}/usr/lib/"
export PATH=$PATH:$PREBUILT/bin
$ICU_SOURCES/source/configure --with-cross-build=${ICU_CROSS_BUILD} \
--enable-extras=yes --enable-strict=no --enable-static --enable-shared=no \
--enable-tests=no --enable-samples=no --enable-dyload=no \
--host=${PREFIX} --prefix=${INSTALL_LOC}
make -j4
make install 