# Demo project to hide Android secrets

Goal of this project is to show how to hide secrets in Android NDK.

Inspired by https://www.splinter.com.au/2014/09/16/storing-secret-keys/

It uses a combination of obfuscation techniques to do so :
- secret is obfuscated using the reversible XOR operator so it never appears in plain sight,
- obfuscated secret is stored in a NDK binary as an hexadecimal array, so it is really hard to spot / put together from a disassembly,
- the obfuscating string is not persisted in the binary to force runtime evaluation (ie : prevent the compiler from disclosing the secret by optimizing the deobfuscation logic).

# Implement your own

## 1 - Generate the HEX array with obfuscated secret

We implemented a Ruby utility to generate the hexadecimal array obfuscating the key :

```shell
    secret_hex_array_generator.rb <SECRET_TO_BE_HIDDEN> <ANDROID_APP_PACKAGE_NAME>
```

## 2 - Persist the secret & get it at runtime

Append the JAVA wrapping function to `secrets.java` :

```kotlin
    class Secrets{
        //...
        external fun getMySecret(packageName: String): String
    }
```

Append the C++ decoding function to `secrets.cpp` :

```cpp
    extern "C"
    JNIEXPORT jstring JNICALL
    Java_com_klaxit_HiddenSecrets_Secrets_getMySecret(
            JNIEnv* pEnv,
            jobject pThis,
            jstring packageName) {
         char obfuscatedSecret[] = <OBFUSCATED_SECRET_ARRAY_GOES_HERE> ;
         return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
    }
```

# Useful tools

Disassembler : https://github.com/radareorg/cutter
