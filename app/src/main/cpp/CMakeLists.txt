cmake_minimum_required(VERSION 3.4.1)

link_directories(../../../jniLibs/${ANDROID_ABI})
set(fluidsynth_sources
        fluidsynth.h
        native-lib.cpp # This line needs to be removed; It's only for testing purposes
)

add_library(fluidsynthjni SHARED ${fluidsynth_sources})
target_link_libraries(fluidsynthjni fluidsynth fluidsynth-assetloader)

if(ANDROID_ABI STREQUAL "armeabi-v7a")
        target_compile_options(fluidsynthjni PRIVATE -DWITH_FLOAT)
endif()
if(ANDROID_ABI STREQUAL "x86")
        target_compile_options(fluidsynthjni PRIVATE -DWITH_FLOAT)
endif()
