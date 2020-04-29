//
// Created by Cyrille Courtière on 2020-04-29.
//

#include "secrets.h"

#include <jni.h>
#include <string>

#include "sha256.h"
#include "sha256.cpp"

jstring getOriginalKey(
        char* obfuscatedKey,
        int obfuscatedKeySize,
        jstring obfuscatingJStr,
        JNIEnv* pEnv) {

    std::string obfuscatingStr = std::string(pEnv->GetStringUTFChars(obfuscatingJStr, NULL));
    std::string obfuscator = sha256(obfuscatingStr);

    char out[obfuscatedKeySize + 1];
    for(int i=0; i < obfuscatedKeySize; i++){
        out[i] = obfuscatedKey[i] ^ obfuscator[i % obfuscator.length()];
    }

    // terminal char required by UTF-8
    out[obfuscatedKeySize] = 0x0;

    return pEnv->NewStringUTF(out);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_klaxit_HiddenSecrets_Secrets_getWellHiddenSecret(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedKey[] = {
             0x23, 0x18, 0x13, 0x56, 0x5f, 0x59, 0x14, 0x5d, 0x5a, 0x1, 0x53, 0x3, 0xd, 0x15,
             0x10, 0x52, 0x5b, 0x46, 0x50, 0x10, 0x16, 0x2, 0x4b, 0x1e
     };
     return getOriginalKey(obfuscatedKey, sizeof(obfuscatedKey), packageName, pEnv);
}
