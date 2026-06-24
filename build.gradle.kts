import java.time.Instant

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version ("8.1.1") apply (false)
}

subprojects {
    if (project.name.contains("Android")) {
        return@subprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "fr.redstonneur1256"
    version = System.getenv("GITHUB_VERSION") ?: "dev"

    java {
//        withJavadocJar()
//        withSourcesJar()
//        toolchain {
//            languageVersion.set(JavaLanguageVersion.of(8))
//        }
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")

        maven("https://repo.spongepowered.org/repository/maven-public/")

        ivy {
            url = uri("https://github.com/")
            patternLayout {
                artifact("/[organisation]/[module]/releases/download/[revision]/dependencies.jar")
            }
            metadataSources {
                artifact()
            }
        }
        ivy {
            url = uri("https://github.com/")
            patternLayout {
                artifact("/[organisation]/[module]/releases/[revision]/download/dependencies.jar")
            }
            metadataSources {
                artifact()
            }
        }
    }

    sourceSets {
        main {
            java.srcDirs("src")
            resources.srcDirs("res")
        }
        test {
            java.srcDirs()
            resources.srcDirs()
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    tasks.processResources {
        filesMatching(listOf("*.properties", "mod.json")) {
            expand(
                mapOf(
                    "version" to version,
                    "build" to (System.getenv("GITHUB_SHA") ?: "dev"),
                    "built" to Instant.now().toString()
                )
            )
        }
        outputs.upToDateWhen {
            false
        }
    }
}
