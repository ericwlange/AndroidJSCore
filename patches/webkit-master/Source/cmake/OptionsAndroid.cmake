set(PROJECT_VERSION_MAJOR 2)
set(PROJECT_VERSION_MINOR 0)
set(PROJECT_VERSION_PATCH 0)
set(PROJECT_VERSION ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}.${PROJECT_VERSION_PATCH})

#set(DATA_DIR ${CMAKE_INSTALL_PREFIX}/share/${PROJECT_NAME}-${PROJECT_VERSION})


#add_definitions(-DDATA_DIR="${DATA_DIR}")

#add_definitions(-DWEBCORE_NAVIGATOR_VENDOR="Appcelerator Inc.")
add_definitions(-DBUILDING_ANDROID__=1)
#add_definitions(-DBUILD_WEBKIT)

message("CMAKE_FIND_ROOT_PATH ${CMAKE_FIND_ROOT_PATH}")

find_package(ICU REQUIRED)
set(WTF_USE_ICU_UNICODE 1)

message("ICU_INCLUDE_DIRS ${ICU_INCLUDE_DIRS}")

set(glib_components gio gobject gthread gmodule)
if (ENABLE_GAMEPAD_DEPRECATED OR ENABLE_GEOLOCATION)
    list(APPEND glib_components gio-unix)
endif ()
find_package(GLIB 2.36 REQUIRED COMPONENTS ${glib_components})
