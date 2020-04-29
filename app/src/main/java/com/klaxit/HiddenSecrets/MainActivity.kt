package com.klaxit.HiddenSecrets

import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.TextView
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.Objects.hash

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Reproduce FB SDK insecure "client authentication"
        // It base client authentication on the app the app "key hash", see :
        // - FB literature on the subject : https://developers.facebook.com/docs/facebook-login/android
        // - FB SDK code : https://git.io/JfYIw
        // This mechan`ism in insecure as key hash in no secret, it can be easily retrieved externally using :
        // `keytool -list -printcert -jarfile <APK_FILE> | grep "SHA1: " | cut -d " " -f 3 | xxd -r -p | openssl base64
        val signatures = packageManager
                            .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                            .signingInfo
                            .apkContentsSigners
        val digest = MessageDigest.getInstance("SHA")
        digest.update(signatures[0].toByteArray())
        Log.i(
            this.localClassName,
            "THIS IS WHAT FB SDK WILL LOOK FOR (INSECURE)" + "\n" +
                    "Signatures count : " + signatures.size + "\n" +
                    "First signature str : \n" +signatures[0].toCharsString() + "\n" +
                    "First signature hex : \n" + bytesToHex(digest.digest())
        )

        // Great resume on how to obfuscate private string in an app :
        // https://stackoverflow.com/a/14572051/807442

        // And the application here
        val textView: TextView = findViewById(R.id.content) as TextView
        textView.setText(Secrets().getWellHiddenSecret("com.klaxit.HiddenSecrets"))
    }

    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = HEX_ARRAY[v.ushr(4)]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
}
