#set ANDROID_NDK_ROOT
#set SRC_ROOT
#set CONFIG_DIR
#set PREBUILT
#set ARCH
#set ABI
#set INSTALL_LOC
#set PREFIX

set -e

export API_LEVEL=21
export PLATFORM=$ANDROID_NDK_ROOT/platforms/android-$API_LEVEL/${ARCH}
export CFLAGS="-fPIC -DANDROID -Wno-multichar -I${INSTALL_LOC}/include -I${INSTALL_LOC}/lib/libffi-3.0.13/include -I${ANDROID_NDK_ROOT}/sources/android/support/include -Wno-error=implicit-function-declaration"
export CPPFLAGS="-I${PLATFORM}/usr/include/ -I${INSTALL_LOC}/include -I${INSTALL_LOC}/lib/libffi-3.0.13/include -DANDROID -DNO_XMALLOC -mandroid -I${ANDROID_NDK_ROOT}/sources/android/support/include -Wno-error=implicit-function-declaration"
export LIBS="-lc "
export LDFLAGS="-Wl,-rpath-link=${PLATFORM}/usr/lib/ -L${INSTALL_LOC}/lib -L${INSTALL_LOC}/lib64 -L${PLATFORM}/usr/lib"

export CROSS_COMPILE=${PREBUILT}/bin/${PREFIX}
export CC="${CROSS_COMPILE}-gcc --sysroot=$PLATFORM"
export CXX="${CROSS_COMPILE}-g++ --sysroot=$PLATFORM"
export AR=${CROSS_COMPILE}-ar
export AS=${CROSS_COMPILE}-as
export LD=${CROSS_COMPILE}-ld
export RANLIB=${CROSS_COMPILE}-ranlib
export NM=${CROSS_COMPILE}-nm
export STRIP=${CROSS_COMPILE}-strip

mkdir -p ${INSTALL_LOC}
mkdir -p ${INSTALL_LOC}/include

cd ${ABI}
mkdir -p libiconv-1.14
cd libiconv-1.14
${SRC_ROOT}/libiconv-1.14/configure --build=x86_64-unknown-linux-gnu --host=${HOST} --disable-rpath --prefix=${INSTALL_LOC}
make -j4
make install

mkdir -p ../libffi-3.0.13
cd ../libffi-3.0.13
${SRC_ROOT}/libffi-3.0.13/configure --build=x86_64-unknown-linux-gnu --host=${PREFIX} --prefix=${INSTALL_LOC} --enable-static
make -j4
make install

mkdir -p ../gettext-0.18.3
cd ../gettext-0.18.3
${SRC_ROOT}/gettext-0.18.3/configure --build=x86_64-unknown-linux-gnu --host=${HOST} --disable-rpath --prefix=${INSTALL_LOC} --disable-libasprintf --disable-java --disable-native-java --disable-openmp --disable-curses
sed -i.bak '/_prg_LDADD_1 = -lpthread/d' ./gettext-tools/tests/Makefile
make -j4
make install

export LIBFFI_CFLAGS=-I${INSTALL_LOC}/include
export LIBFFI_LIBS="-L${INSTALL_LOC}/lib -lffi"

export PATH=$PATH:${INSTALL_LOC}/bin

mkdir -p ../glib-2.37.93
cd ../glib-2.37.93
cp ${CONFIG_DIR}/android.cache .
${SRC_ROOT}/glib-2.37.93/configure --build=x86_64-unknown-linux-gnu --host=${PREFIX} --prefix=${INSTALL_LOC} --disable-dependency-tracking --cache-file=android.cache --enable-included-printf --enable-static
make -j4
make install
