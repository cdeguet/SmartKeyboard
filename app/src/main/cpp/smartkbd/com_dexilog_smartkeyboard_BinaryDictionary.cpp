/*
**
** Copyright 2009, The Android Open Source Project
** Copyright 2009, Spiros Papadimitriou <spapadim@cs.cmu.edu>
** Copyright (C) 2010-2017 Cyril Deguet

**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#define LOG_TAG "BinaryDictionary"

#include <jni.h>

#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <fcntl.h>

#include "dictionary.h"
#include "expandable_dic.h"

using namespace smartkbd;

static jfieldID sDescriptorField;

//
// helper function to throw an exception
//
static void throwException(JNIEnv *env, const char* ex, const char* fmt, int data)
{
    if (jclass cls = env->FindClass(ex)) {
        char msg[1000];
        sprintf(msg, fmt, data);
        env->ThrowNew(cls, msg);
        env->DeleteLocalRef(cls);
    }
}

static jlong smartkbd_BinaryDictionary_open
        (JNIEnv *env, jobject object, jobject fileDescriptor,
         jlong offset, jlong length,
         jint typedLetterMultiplier, jint fullWordMultiplier)
{
    jint fd = env->GetIntField(fileDescriptor, sDescriptorField);

    unsigned char *dict = new unsigned char[length];
    if (dict == NULL) {
        fprintf(stderr, "DICT: Failed to allocate dictionary buffer\n");
        return 0;
    }

    lseek(fd, offset, SEEK_SET);
    size_t bytesLeft = length;
    unsigned char *p = dict;
    while (bytesLeft > 0) {
        size_t bytesRead = read(fd, p, bytesLeft);
        p += bytesRead;
        bytesLeft -= bytesRead;
    }
    // FIXME check: need to close fd?

    Dictionary *dictionary = new Dictionary(dict, typedLetterMultiplier, fullWordMultiplier);

    return reinterpret_cast<jlong>(dictionary);
}

static int smartkbd_BinaryDictionary_getSuggestions(
        JNIEnv *env, jobject object, jlong dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords,
        jint maxAlternatives, jint skipPos, jboolean modeT9, 
		jintArray nextLettersArray, jint nextLettersSize)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL)
        return 0;

    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);
    int *nextLetters = nextLettersArray != NULL ? env->GetIntArrayElements(nextLettersArray, NULL)
            : NULL;

    int count = dictionary->getSuggestions(inputCodes, arraySize, (unsigned short*) outputChars, frequencies,
            maxWordLength, maxWords, maxAlternatives, skipPos, modeT9, nextLetters, nextLettersSize);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);
    if (nextLetters) {
        env->ReleaseIntArrayElements(nextLettersArray, nextLetters, 0);
    }

    return count;
}

static jboolean smartkbd_BinaryDictionary_isValidWord
        (JNIEnv *env, jobject object, jlong dict, jcharArray wordArray, jint wordLength)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return (jboolean) false;

    jchar *word = env->GetCharArrayElements(wordArray, NULL);
    jboolean result = dictionary->isValidWord((unsigned short*) word, wordLength);
    env->ReleaseCharArrayElements(wordArray, word, JNI_ABORT);

    return result;
}

static void smartkbd_BinaryDictionary_close
        (JNIEnv *env, jobject object, jlong dict)
{
    Dictionary *dictionary = (Dictionary*) dict;
    delete dictionary->getDictBuffer();
    delete (Dictionary*) dict;
}


static jlong smartkbd_BinaryDictionary_openExpandable
        (JNIEnv *env, jobject object)
{
    ExpandableDictionary *dictionary = new ExpandableDictionary();
    return reinterpret_cast<jlong>(dictionary);
}

static void smartkbd_BinaryDictionary_addWordExpandable
        (JNIEnv *env, jobject object, jlong dict, jstring word, jint freq)
{
    ExpandableDictionary *dictionary = (ExpandableDictionary*)dict;
	const jchar *wordString = env->GetStringChars(word, 0);
	int len = env->GetStringLength(word);
	dictionary->addWord(wordString, len, freq);
	env->ReleaseStringChars(word, wordString);
}

static void smartkbd_BinaryDictionary_addCharArrayExpandable
        (JNIEnv *env, jobject object, jlong dict, jcharArray word, jint size, jint freq)
{
    ExpandableDictionary *dictionary = (ExpandableDictionary*)dict;
    jchar *wordString = env->GetCharArrayElements(word, NULL);
	dictionary->addWord(wordString, size, freq);
	env->ReleaseCharArrayElements(word, wordString, JNI_ABORT);
}

static jint smartkbd_BinaryDictionary_getWordFrequencyExpandable
        (JNIEnv *env, jobject object, jlong dict, jstring word)
{
    ExpandableDictionary *dictionary = (ExpandableDictionary*)dict;
	const jchar *wordString = env->GetStringChars(word, 0);
	int len = env->GetStringLength(word);
	int ret = dictionary->getWordFrequency(wordString, len);
	env->ReleaseStringChars(word, wordString);
	return ret;
}

static jint smartkbd_BinaryDictionary_increaseWordFrequencyExpandable
        (JNIEnv *env, jobject object, jlong dict, jstring word)
{
    ExpandableDictionary *dictionary = (ExpandableDictionary*)dict;
	const jchar *wordString = env->GetStringChars(word, 0);
	int len = env->GetStringLength(word);
	int ret = dictionary->increaseWordFrequency(wordString, len);
	env->ReleaseStringChars(word, wordString);
	return ret;
}

static int smartkbd_BinaryDictionary_getSuggestionsExpandable(
        JNIEnv *env, jobject object, jlong dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords,
        jint maxAlternatives, jint skipPos, jboolean modeT9,
		jintArray nextLettersArray, jint nextLettersSize)
{
    ExpandableDictionary *dictionary = (ExpandableDictionary*) dict;

    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);
    int *nextLetters = nextLettersArray != NULL ? env->GetIntArrayElements(nextLettersArray, NULL)
            : NULL;

    int count = dictionary->getSuggestions(inputCodes, arraySize, (unsigned short*) outputChars, frequencies,
            maxWordLength, maxWords, maxAlternatives, skipPos, modeT9, nextLetters, nextLettersSize);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);
    if (nextLetters) {
        env->ReleaseIntArrayElements(nextLettersArray, nextLetters, 0);
    }

    return count;
}

static void smartkbd_BinaryDictionary_closeExpandable
        (JNIEnv *env, jobject object, jlong dict)
{
    ExpandableDictionary *dictionary = (ExpandableDictionary*) dict;
    delete dictionary;
}


// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"openNative",           "(Ljava/io/FileDescriptor;JJII)J",
                                                (void*)smartkbd_BinaryDictionary_open},
    {"closeNative",          "(J)V",            (void*)smartkbd_BinaryDictionary_close},
    {"getSuggestionsNative", "(J[II[C[IIIIIZ[II)I", (void*)smartkbd_BinaryDictionary_getSuggestions},
    {"isValidWordNative",    "(J[CI)Z",         (void*)smartkbd_BinaryDictionary_isValidWord},

    {"openExpandableNative",        "()J",                        (void*)smartkbd_BinaryDictionary_openExpandable},
    {"addWordExpandableNative",     "(JLjava/lang/String;I)V",    (void*)smartkbd_BinaryDictionary_addWordExpandable},
    {"addCharArrayExpandableNative",     "(J[CII)V",               (void*)smartkbd_BinaryDictionary_addCharArrayExpandable},
    {"getWordFrequencyExpandableNative", "(JLjava/lang/String;)I",    	(int*)smartkbd_BinaryDictionary_getWordFrequencyExpandable},
    {"increaseWordFrequencyExpandableNative", "(JLjava/lang/String;)I",(int*)smartkbd_BinaryDictionary_increaseWordFrequencyExpandable},
    {"getSuggestionsExpandableNative", "(J[II[C[IIIIIZ[II)I",     (void*)smartkbd_BinaryDictionary_getSuggestionsExpandable},
    {"closeExpandableNative",       "(J)V",                       (void*)smartkbd_BinaryDictionary_closeExpandable},
};

static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        fprintf(stderr,
            "Native registration unable to find class '%s'\n", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        fprintf(stderr, "RegisterNatives failed for '%s'\n", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Returns the JNI version on success, -1 on failure.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    const char* const kClassPathName = "com/dexilog/smartkeyboard/BinaryDictionary";

    JNIEnv* env = NULL;
    jclass clazz;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        fprintf(stderr, "ERROR: GetEnv failed\n");
        return -1;
    }
    assert(env != NULL);

    clazz = env->FindClass("java/io/FileDescriptor");
    if (clazz == NULL) {
        fprintf(stderr, "Can't find %s", "java/io/FileDescriptor");
        return -2;
    }
    sDescriptorField = env->GetFieldID(clazz, "descriptor", "I");

    if (!registerNativeMethods(env,
            kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
        fprintf(stderr, "ERROR: BinaryDictionary native registration failed\n");
        return -3;
    }

    /* success -- return valid version number */
    return JNI_VERSION_1_6;
}
