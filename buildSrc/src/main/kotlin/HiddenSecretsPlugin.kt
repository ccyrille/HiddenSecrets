import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.OutputFile
import java.io.File
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.experimental.xor

open class HiddenSecretsPluginExtension {
    var packageName: String = "must be replaced"
    var destination: String = "folder"
}

open class HiddenSecretsPlugin : Plugin<Project> {
    companion object {
        private const val BUILDSRC_MAIN_FOLDER = "buildSrc/src/main/"
        private const val APP_MAIN_FOLDER = "app/src/main/"

        private const val TASK_COPY_CPP = "copyCpp"
    }


    override fun apply(project: Project) {

        val hiddenExtension = project.extensions.create<HiddenSecretsPluginExtension>(
            "hidden", HiddenSecretsPluginExtension::class.java
        )

        project.task("hidden") {
            doLast {
                println("HIDDEN TASK")



                println("Hidden secrets plugin START")
                println("Will use package " + hiddenExtension.packageName)
            }
        }

        @OutputFile
        fun getCppDestination(fileName: String): File {
            return project.file(APP_MAIN_FOLDER + "cpp2/$fileName")
        }

        @OutputFile
        fun getJavaDestination(packageName: String, fileName: String): File {
            var path = APP_MAIN_FOLDER + "java/"
            packageName.split(".").forEach {
                path += "$it/"
            }
            path += fileName
            println("Java destination = $path")
            return project.file(path)
        }

        project.task("generateKey") {
            doLast {
                println("generateKey")

                //TODO
            }
        }

        project.task(TASK_COPY_CPP) {
            doLast {
                project.file(BUILDSRC_MAIN_FOLDER + "cpp/").listFiles()?.forEach {
                    val destination = getCppDestination(it.name)
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        project.task("createJavaClass") {
            doLast {
                println("TASK createJavaClass")
                project.file(BUILDSRC_MAIN_FOLDER + "java/").listFiles()?.forEach {
                    println(it.absolutePath)
                    val destination = getJavaDestination(hiddenExtension.packageName, it.name)
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        fun sha256(toHash: String): String {
            val bytes = toHash.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("", { str, it -> str + "%02x".format(it) })
        }

        fun encode(key: String, packageName: String): String {
            //Generate the obfuscator as the SHA256 of the app package name
            val obfuscator = sha256(packageName)
            //println("SHA 256 = " + obfuscator)
            val obfuscatorBytes = obfuscator.toByteArray()

            //Generate the obfuscated secret bytes array by applying a XOR between the secret and the obfuscator
            val obfuscatedSecretBytes = arrayListOf<Byte>()
            var i = 0
            key.toByteArray(Charset.defaultCharset()).forEach { secretByte ->
                val obfuscatorByte = obfuscatorBytes[i % obfuscatorBytes.size]
                val obfuscatedByte = secretByte.xor(obfuscatorByte)
                obfuscatedSecretBytes.add(obfuscatedByte)
                i++
            }
            var encoded = "{ "
            val iterator: Iterator<Byte> = obfuscatedSecretBytes.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                encoded += "0x" + Integer.toHexString(item.toInt())
                if (iterator.hasNext()) {
                    encoded += ", "
                }
            }
            encoded += " }"

            return encoded
        }

        project.task("replaceKey") {
            doLast {
                var key = ""
                if (project.hasProperty("key")) {
                    //From command line
                    key = project.property("key") as String
                }
                println("### SECRET ###\n$key\n")

                var packageName = hiddenExtension.packageName
                if (project.hasProperty("package")) {
                    //From command line
                    packageName = project.property("package") as String
                }
                println("### PACKAGE NAME ###\n$packageName\n")

                val encodedKey = encode(key, packageName)
                println("### OBFUSCATED SECRET ###\n$encodedKey")

                //Replace key in Cpp
                val file = getCppDestination("secrets.cpp")
                if (file.exists()) {
                    var text = file.readText(Charset.defaultCharset())
                    text = text.replace("{YOUR_KEY_GOES_HERE}", encodedKey)
                    file.writeText(text)
                } else {
                    error("Missing C++ file, please run gradle task : $TASK_COPY_CPP")
                }
            }
        }
    }
}