pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    // NewPipe Extractor-এর জন্য JitPack যোগ করুন
    maven(url = "https://jitpack.io")
  }
}

rootProject.name = "StreamMates"

include(":app")