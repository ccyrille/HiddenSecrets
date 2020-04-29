# Demo project to hide Android secrets

Goal of this project is to show how to hide secrets in Android NDK.

Inspired by https://www.splinter.com.au/2014/09/16/storing-secret-keys/

It uses a combination of obfuscation techniques to do so :
- secret is obfuscated using the reversible XOR operator so it never appears in plain sight
- the other factor of the XOR operator is not persisted in the binary to force runtime evaluation (prevent any compiler optimization)
- obfuscated secret is store as an hexadecimal array in the compiled binary so it is really difficult to spot / put together from a disassembly

# Implement your own

## 1 - Generate the HEX array with obfuscated secret

We implemented a Ruby utility to generate the hexadecimal array obfuscating the key :

```shell
    secret_hex_array_generator.rb <SECRET_TO_BE_HIDDEN> <ANDROID_APP_PACKAGE_NAME>
```

## 2 - Persist the secret & get it at runtime

```kotlin
    class Secrets{
        //...
        external fun getMySecret(packageName: String): String
    }
```

If the obfuscated key is 39 chars long...

```cpp
    extern "C"
    JNIEXPORT jstring JNICALL
    Java_com_klaxit_HiddenSecrets_Secrets_getMySecret(
            JNIEnv* pEnv,
            jobject pThis,
            jstring packageName) {
         char obfuscatedKey[39] = <OBFUSCATED_ARRAY_GOES_HERE>
        std::string packageNameStr = std::string(pEnv->GetStringUTFChars(packageName, NULL));
        std::string packageNameSha256 = sha256(packageNameStr);
        int packageNameSha256Size = packageNameSha256.length();
        char out[40];
        for(int i=0; i < sizeof(obfuscatedKey); i++){
            out[i] = obfuscatedKey[i] ^ packageNameSha256[i % packageNameSha256Size];
        }
        out[39] = 0x0; // terminal char required by UTF-8
        return pEnv->NewStringUTF(out);
    }
```

# Useful tools

Disassembler : https://github.com/radareorg/cutter
