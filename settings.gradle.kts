rootProject.name = "Mindustry-ModLib"

val androidSdkPath: String? = System.getenv("ANDROID_HOME")
if(androidSdkPath != null && file(androidSdkPath).isDirectory && file("Android").isDirectory) {
    pluginManagement {
        repositories {
            google()
            mavenCentral()
            gradlePluginPortal()
        }
    }

    include("Android")
}

include("Common")
include("Example")
include("Launcher")
include("Mod")
