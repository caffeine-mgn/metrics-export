plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("com.github.johnrengelman.shadow").version("5.2.0")
}
val binomVersion = project.property("binom.version")
val nativeEntryPoint = "pw.binom.main"
description = "metric-exporter"
val mainClassName = "pw.binom.MainKt"
kotlin {
    jvm()
    linuxArm64 {
        binaries {
            executable {
                entryPoint = nativeEntryPoint
            }
        }
    }
    linuxX64 {
        binaries {
            executable {
                entryPoint = nativeEntryPoint
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            api("pw.binom.io:httpServer:$binomVersion")
            api("pw.binom.io:httpClient:$binomVersion")
            api("pw.binom.io:signal:$binomVersion")
            api("pw.binom.io:strong-web-server:$binomVersion")
            api("pw.binom.io:strong-properties:$binomVersion")
            api("pw.binom.io:prometheus:$binomVersion")
            api("pw.binom.io:coroutines:$binomVersion")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${org.tlsys.Versions.KOTLINX_COROUTINES_VERSION}")
            api("org.jetbrains.kotlinx:kotlinx-serialization-core:${org.tlsys.Versions.KOTLINX_SERIALIZATION_VERSION}")
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:${org.tlsys.Versions.KOTLINX_SERIALIZATION_VERSION}")
        }
        commonTest.dependencies {
            api("pw.binom.io:testing:$binomVersion")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${org.tlsys.Versions.KOTLINX_COROUTINES_VERSION}")
        }
        jvmTest.dependencies {
            api(kotlin("test-junit"))
        }
    }
}
//tasks {
//    this.withType(Jar::class).configureEach {
//        this.manifest {
//            attributes("Main-Class" to mainClassName)
//        }
//    }
//}
tasks{
    val jvmJar by getting(Jar::class)

    val shadowJar by creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        from(jvmJar.archiveFile)
        group = "build"
        configurations = listOf(project.configurations["jvmRuntimeClasspath"])
        exclude(
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/*.txt",
            "META-INF/NOTICE",
            "LICENSE",
        )
        manifest {
            attributes("Main-Class" to mainClassName)
        }
    }
}
apply<org.tlsys.DockerPackJvm>()
