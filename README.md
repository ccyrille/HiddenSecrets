# Deeply hide secrets on Android

Minimal project to demonstrate a method to **deeply hide a secret on Android**.

It is highly inspired from https://www.splinter.com.au/2014/09/16/storing-secret-keys/

It uses a combination of obfuscation techniques to do so :
- secret is obfuscated using the reversible XOR operator so it never appears in plain sight,
- obfuscated secret is stored in a NDK binary as an hexadecimal array, so it is really hard to spot / put together from a disassembly,
- the obfuscating string is not persisted in the binary to force runtime evaluation (ie : prevent the compiler from disclosing the secret by optimizing the de-obfuscation logic).

⚠️ Nothing on the client-side is unbreakable. So generally speaking, **keeping a secret in a mobile package is not a smart idea**. But when you absolutely need to, this is the best method we have found to hide it.

# Hide your own secret

Here are the steps to follow to hide your own secret.

## 1 - Generate the obfuscated secret

We implemented a Ruby utility to generate the hexadecimal array obfuscating the secret :

```shell
    secret_hex_array_generator.rb <SECRET_TO_BE_HIDDEN> <ANDROID_APP_PACKAGE_NAME>
```

## 2 - Persist the secret in your code

### 2.1 - Copy required files in your project
Copy the `Secrets.kt` file in your project.
Copy the `cpp` folder in your project, in the `main` directory (next to the `java` folder).

### 2.2 - Edit the decoding function in `app/src/main/cpp/secrets.cpp`
#### Edit the function name
In the function name `com_klaxit_HiddenSecrets` must be replaced by the package name where your placed the file `Secrets.kt` in your project.
#### Place your own generated secret array
Replace `<OBFUSCATED_SECRET_ARRAY_GOES_HERE>` by the generate array from the step 1

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

## 3 - Setup gradle to be able to build the added C++ code

In your gradle add thoses line at the same level as `buildTypes` :

```
externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
        }
    }
    ```

## 4 - Display the secret at runtime

In any class you can now get your secret key, for example to display it in a textView :

```kotlin
    textView.setText(Secrets().getMySecret(<ANDROID_APP_PACKAGE_NAME>))
```

## 5 - Build & run your app

Build & run your project from Android Studio... Et voilà!

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
