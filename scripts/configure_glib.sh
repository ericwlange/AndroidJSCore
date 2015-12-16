#set ANDROID_NDK_ROOT
#set SRC_ROOT
#set CONFIG_DIR
#set PREBUILT // $ANDROID_NDK_ROOT/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64/
#set ARCH // arch-arm
#set ABI // armeabi
#set INSTALL_LOC // $HOME/android-glib
#set PREFIX // arm-linux-androideabi

export API_LEVEL=21
export PLATFORM=$ANDROID_NDK_ROOT/platforms/android-$API_LEVEL/${ARCH}
#export CFLAGS="-fPIC -DANDROID -nostdlib -Wno-multichar"
export CFLAGS="-fPIC -DANDROID -Wno-multichar -I${INSTALL_LOC}/include -I${INSTALL_LOC}/lib/libffi-3.0.13/include"
export CPPFLAGS="-I${PLATFORM}/usr/include/ -I${INSTALL_LOC}/include -I${INSTALL_LOC}/lib/libffi-3.0.13/include -DANDROID -DNO_XMALLOC -mandroid"
export LIBS="-lc "
export LDFLAGS="-Wl,-rpath-link=${PLATFORM}/usr/lib/ -L${INSTALL_LOC}/lib -L${PLATFORM}/usr/lib"

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

cd ${ABI}
mkdir -p libiconv-1.14
cd libiconv-1.14
${SRC_ROOT}/libiconv-1.14/configure --build=x86_64-unknown-linux-gnu --host=arm-eabi --disable-rpath --prefix=${INSTALL_LOC}
make
make install

mkdir -p ../libffi-3.0.13
cd ../libffi-3.0.13
${SRC_ROOT}/libffi-3.0.13/configure --build=x86_64-unknown-linux-gnu --host=arm-eabi --prefix=${INSTALL_LOC} --enable-static
make
make install

mkdir -p ../gettext-0.18.3
cd ../gettext-0.18.3
${SRC_ROOT}/gettext-0.18.3/configure --build=x86_64-unknown-linux-gnu --host=arm-eabi --disable-rpath --prefix=${INSTALL_LOC} --disable-libasprintf --disable-java --disable-native-java --disable-openmp --disable-curses
make
make install

mkdir -p ../glib-2.37.92
cd ../glib-2.37.92
${SRC_ROOT}/glib-2.37.92/configure --build=x86_64-unknown-linux-gnu --host=arm-linux-androideabi --prefix=${INSTALL_LOC} --disable-dependency-tracking --cache-file=${CONFIG_DIR}/android.cache --enable-included-printf --enable-static
make
make install

