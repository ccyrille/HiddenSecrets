package com.klaxit.HiddenSecrets;

public class Secrets {

    external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
            System.loadLibrary("secrets")
        }
    }
}
