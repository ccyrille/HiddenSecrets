package com.klaxit.HiddenSecrets;

/**
 * Copy this file in your project to access the C++ decoding function
 */
class Secrets {

    external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
            System.loadLibrary("secrets")
        }
    }
}
