#include "secrets.hpp"

#include <jni.h>
#include <string>

#include "sha256.hpp"
#include "sha256.cpp"

/* Copyright (c) 2020-present Klaxit SAS
*
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
*
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
* OTHER DEALINGS IN THE SOFTWARE.
*/

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

    // Add string terminal delimiter
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
