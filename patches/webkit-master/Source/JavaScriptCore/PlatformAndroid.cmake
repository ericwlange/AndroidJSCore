SET(GCC_JSC_COMPILE_FLAGS "-D__STDC_LIMIT_MACROS=1 -DUCONFIG_ONLY_COLLATION=1")

# Disable JIT and assembly on older processors
if( ANDROID_ABI STREQUAL "armeabi" OR ANDROID_ABI STREQUAL "mips" )
    SET(GCC_JSC_COMPILE_FLAGS "${GCC_JSC_COMPILE_FLAGS} -DENABLE_JIT=0 -DENABLE_YARR_JIT=0 -DENABLE_ASSEMBLER=0")    
endif()

SET( CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} ${GCC_JSC_COMPILE_FLAGS}" )

FIND_LIBRARY( ANDROID_LOG_LIBRARY log )
FIND_LIBRARY( ANDROID_CPPSTD_LIBRARY stdc++ )

#        FIND_LIBRARY( ANDROID_ANDROID_LIBRARY android )

# Because Android has multiple architectures (e.g. armeabi, armeabi-v7a, x86)
# but doesn't have fat binaries like Darwin, nor does it use file suffixes,
# we need to override the library install path to
# put products in the correct architecture specific subdirectory.
# -DANDROID_ABI=<arch> should have been specified on cmake invocation.
set(LIB_INSTALL_DIR "${ANDROID_ABI}")

list(APPEND JavaScriptCore_SOURCES
    android/JSContext.cpp
    android/JSObject.cpp
    android/JSString.cpp
    android/JSValue.cpp
)

list(APPEND JavaScriptCore_LIBRARIES
    ${ANDROID_LOG_LIBRARY}
    ${ANDROID_CPPSTD_LIBRARY}
#    ${ANDROID_ANDROID_LIBRARY}
    ${ICU_LIBRARIES}
    ${ICU_I18N_LIBRARIES}
    WTF${DEBUG_SUFFIX}
)

list(APPEND JavaScriptCore_SYSTEM_INCLUDE_DIRECTORIES
    ${GLIB_INCLUDE_DIRS}
    ${ICU_INCLUDE_DIRS}
    ${WTF_DIR}
)

install(FILES DESTINATION)

