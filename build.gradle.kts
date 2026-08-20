plugins {
    id("java-library")
    id("com.diffplug.spotless") version "6.25.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "com.smileidentity"
version = "12.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okhttp3:okhttp-tls:4.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Compilation always targets Java 11. Tests run on Java 11 by default and CI
// also runs them on a newer JVM with -PtestJavaVersion, so the compatibility
// range is covered without tying the Gradle daemon to an old JVM.
tasks.test {
    useJUnitPlatform()
    javaLauncher.set(
        javaToolchains.launcherFor {
            val requested = providers.gradleProperty("testJavaVersion").getOrElse("11")
            languageVersion.set(JavaLanguageVersion.of(requested.toInt()))
        },
    )
}

spotless {
    java {
        googleJavaFormat()
        target("src/**/*.java")
    }
}

// Publishes com.smileidentity:usesmileid-java to Maven Central through the Central
// Portal. Coordinates come from group, rootProject.name and version above. The
// plugin adds the sources and javadoc jars Central requires.
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Central rejects unsigned releases. Only sign when a key is present so that
    // publishToMavenLocal still works for contributors who have none.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    pom {
        name.set("Smile ID Java SDK")
        description.set("Official Smile ID server-side SDK for Java.")
        inceptionYear.set("2026")
        url.set("https://github.com/smileidentity/smileid-sdk-java")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/smileidentity/smileid-sdk-java/blob/main/LICENSE")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("smileidentity")
                name.set("Smile Identity")
                url.set("https://github.com/smileidentity")
            }
        }
        scm {
            url.set("https://github.com/smileidentity/smileid-sdk-java")
            connection.set("scm:git:git://github.com/smileidentity/smileid-sdk-java.git")
            developerConnection.set("scm:git:ssh://git@github.com/smileidentity/smileid-sdk-java.git")
        }
    }
}
