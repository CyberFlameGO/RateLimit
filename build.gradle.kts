import java.util.*

plugins {
    kotlin("multiplatform") version "1.6.21"
    id("org.jetbrains.dokka") version "1.6.21"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "app.softwork"

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosArm32()
    iosSimulatorArm64()

    watchosX86()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    macosX64()
    macosArm64()
    linuxX64()

    explicitApi()

    sourceSets {
        // Apache 2, https://github.com/ktorio/ktor/releases/latest
        val ktorVersion = "2.0.1"

        commonMain {
            dependencies {
                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-cors:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))

                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val jvmTest by getting {
            dependencies {
                // Apache 2, https://github.com/JetBrains/Exposed/releases/latest
                val exposedVersion = "0.38.2"
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-auth:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")

                // EPL 1.0, https://github.com/h2database/h2database/releases/latest
                runtimeOnly("com.h2database:h2:2.1.212")
            }
        }
    }
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            moduleName by "RateLimit"
            includes.from("README.md")
            reportUndocumented by true
            val sourceSetName = name
            File("src/$sourceSetName/kotlin").takeIf { it.exists() }?.let {
                sourceLink {
                    localDirectory by it
                    remoteUrl by uri("https://github.com/hfhbd/RateLimit/tree/main/src/$sourceSetName/kotlin").toURL()
                    remoteLineSuffix by "#L"
                }
            }
        }
    }
}

infix fun <T> Property<T>.by(value: T) {
    set(value)
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getProperty("sonartype.apiKey") ?: System.getenv("SONARTYPE_APIKEY"))
            password.set(System.getProperty("sonartype.apiToken") ?: System.getenv("SONARTYPE_APITOKEN"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

val emptyJar by tasks.creating(Jar::class) { }

publishing {
    publications.all {
        this as MavenPublication
        artifact(emptyJar) {
            classifier = "javadoc"
        }
        pom {
            name by "app.softwork RateLimit Library"
            description by "A ratelimit plugin for Ktor"
            url by "https://github.com/hfhbd/RateLimit"
            licenses {
                license {
                    name by "The Apache License, Version 2.0"
                    url by "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id by "hfhbd"
                    name by "Philip Wedemann"
                    email by "mybztg+mavencentral@icloud.com"
                }
            }
            scm {
                connection by "scm:git://github.com/hfhbd/RateLimit.git"
                developerConnection by "scm:git://github.com/hfhbd/RateLimit.git"
                url by "https://github.com/hfhbd/RateLimit"
            }
        }
    }
}

(System.getProperty("signing.privateKey") ?: System.getenv("SIGNING_PRIVATE_KEY"))?.let {
    String(Base64.getDecoder().decode(it)).trim()
}?.let { key ->
    println("found key, config signing")
    signing {
        val signingPassword = System.getProperty("signing.password") ?: System.getenv("SIGNING_PASSWORD")
        useInMemoryPgpKeys(key, signingPassword)
        sign(publishing.publications)
    }
}
