cmake_minimum_required(VERSION 3.4.1)

set(fluidsynth_sources
        fluidsynth.h
        native-lib.cpp # This line needs to be removed; It's only for testing purposes
)

add_library(fluidsynth SHARED ${fluidsynth_sources})

if(ANDROID_ABI STREQUAL "armeabi-v7a")
        target_compile_options(fluidsynth PRIVATE -DWITH_FLOAT)
endif()
if(ANDROID_ABI STREQUAL "x86")
        target_compile_options(fluidsynth PRIVATE -DWITH_FLOAT)
endif()
