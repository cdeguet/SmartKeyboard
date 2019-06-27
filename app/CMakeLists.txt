# Copyright (C) 2010-2017 Cyril Deguet
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             smartkbddict

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/smartkbd/dictionary.cpp
             src/main/cpp/smartkbd/com_dexilog_smartkeyboard_BinaryDictionary.cpp
             src/main/cpp/smartkbd/expandable_dic.cpp)

add_library(smartkbdpinyin
SHARED
src/main/cpp/pinyin/android/com_android_inputmethod_pinyin_PinyinDecoderService.cpp
src/main/cpp/pinyin/share/dictbuilder.cpp
src/main/cpp/pinyin/share/dictlist.cpp
src/main/cpp/pinyin/share/dicttrie.cpp
src/main/cpp/pinyin/share/lpicache.cpp
src/main/cpp/pinyin/share/matrixsearch.cpp
src/main/cpp/pinyin/share/mystdlib.cpp
src/main/cpp/pinyin/share/ngram.cpp
src/main/cpp/pinyin/share/pinyinime.cpp
src/main/cpp/pinyin/share/searchutility.cpp
src/main/cpp/pinyin/share/spellingtable.cpp
src/main/cpp/pinyin/share/spellingtrie.cpp
src/main/cpp/pinyin/share/splparser.cpp
src/main/cpp/pinyin/share/utf16char.cpp
src/main/cpp/pinyin/share/utf16reader.cpp
)

add_library(smartkbdwnn
SHARED
src/main/cpp/libwnnDictionary/OpenWnnDictionaryImplJni.c
src/main/cpp/libwnnDictionary/engine/ndapi.c
src/main/cpp/libwnnDictionary/engine/ndbdic.c
src/main/cpp/libwnnDictionary/engine/ndcommon.c
src/main/cpp/libwnnDictionary/engine/ndfdic.c
src/main/cpp/libwnnDictionary/engine/ndldic.c
src/main/cpp/libwnnDictionary/engine/ndrdic.c
src/main/cpp/libwnnDictionary/engine/neapi.c
src/main/cpp/libwnnDictionary/engine/necode.c
src/main/cpp/libwnnDictionary/engine/nj_str.c
)