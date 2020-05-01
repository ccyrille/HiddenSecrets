//
// Created by Cyrille Courtière on 2020-04-29.
//

#include "secrets.h"

#include <jni.h>
#include <string>

#include "sha256.h"
#include "sha256.cpp"

jstring getOriginalKey(
        char* obfuscatedSecret,
        int obfuscatedSecretSize,
        jstring obfuscatingJStr,
        JNIEnv* pEnv) {

    // Get the obfuscating string SHA256 as the obfuscator
    std::string obfuscatingStr = std::string(pEnv->GetStringUTFChars(obfuscatingJStr, NULL));
    std::string obfuscator = sha256(obfuscatingStr);

    // Apply a XOR between the obfuscated key and the obfuscating string to get original sting
    char out[obfuscatedSecretSize + 1];
    for(int i=0; i < obfuscatedSecretSize; i++){
        out[i] = obfuscatedSecret[i] ^ obfuscator[i % obfuscator.length()];
    }

    // Add terminal char required by UTF-8
    out[obfuscatedSecretSize] = 0x0;

    return pEnv->NewStringUTF(out);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_klaxit_HiddenSecrets_Secrets_getWellHiddenSecret(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedSecret[] = {
             0x23, 0x18, 0x13, 0x56, 0x5f, 0x59, 0x14, 0x5d, 0x5a, 0x1, 0x53, 0x3, 0xd, 0x15,
             0x10, 0x52, 0x5b, 0x46, 0x50, 0x10, 0x16, 0x2, 0x4b, 0x1e
     };
     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}
