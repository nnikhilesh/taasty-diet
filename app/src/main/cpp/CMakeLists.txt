cmake_minimum_required(VERSION 3.22.1)

project("tastydiet")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Define our source files
set(LLAMA_SOURCES
    llama_jni.cpp
)

# Create shared library
add_library(llama_jni SHARED ${LLAMA_SOURCES})

# Include directories
target_include_directories(llama_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Link libraries
target_link_libraries(llama_jni
    android
    log
)

# Compiler flags
target_compile_options(llama_jni PRIVATE
    -Wall
    -Wextra
    -O3
    -DNDEBUG
) 