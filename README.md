# Deeply hide secrets on Android

Minimal project to demonstrate a method to **deeply hide a secret on Android**.

It is highly inspired from https://www.splinter.com.au/2014/09/16/storing-secret-keys/

It uses a combination of obfuscation techniques to do so :
- secret is obfuscated using the reversible XOR operator so it never appears in plain sight,
- obfuscated secret is stored in a NDK binary as an hexadecimal array, so it is really hard to spot / put together from a disassembly,
- the obfuscating string is not persisted in the binary to force runtime evaluation (ie : prevent the compiler from disclosing the secret by optimizing the de-obfuscation logic).

# Hide your own secret

Here are the steps to follow to hide your own secret.

## 1 - Generate the obfuscated secret

We implemented a Ruby utility to generate the hexadecimal array obfuscating the secret :

```shell
    secret_hex_array_generator.rb <SECRET_TO_BE_HIDDEN> <ANDROID_APP_PACKAGE_NAME>
```

In this project `ANDROID_APP_PACKAGE_NAME` will always be `com.klaxit.HiddenSecrets`.

## 2 - Persist the secret

Append the JAVA wrapping function to `app/src/main/java/com/klaxit/HiddenSecrets/Secrets.kt`, for example :

```kotlin
    class Secrets{
        //...
        external fun getMySecret(packageName: String): String
    }
```

Append the C++ decoding function containing the obfuscated secret to `app/src/main/cpp/secrets.cpp` :

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

## 3 - Display the secret at runtime

In `app/src/main/java/com/klaxit/HiddenSecrets/MainActivity.kt`, you will just need to add the following line to 
display your secret :

```kotlin
    textView.setText(Secrets().getMySecret("com.klaxit.HiddenSecrets"))
```

## 4 - Build & run your app

Build & run yhe project from Android Studio... Et voilà!

# Challenge

We have built a small challenge in this project to test the robustness of our implementation. You will
find in `challenge/` an APK containing another hidden secret.

Ping us on our "dev" email address if you happen to find it 😋

## Useful tools

Disassemblers:
- https://github.com/radareorg/cutter
- https://binary.ninja/

# Authors

See the list of [contributors](https://github.com/klaxit/HiddenSecrets/contributors) who participated in this project.

# License

Please see LICENSE

