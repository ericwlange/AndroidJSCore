#set JNI_LIBS
#set AAR_INSTALL

set -e

cd ${JNI_LIBS}/../../../../
./gradlew assembleRelease
mkdir -p ${AAR_INSTALL}
install -C AndroidJSCore/build/outputs/aar/*-release.aar ${AAR_INSTALL} 