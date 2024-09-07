package org.tlsys

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.io.File

open class DockerPackJvm : BasicDockerPlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        val kotlin = project.findKotlin()
        val jarTarget = kotlin.findTarget(KotlinPlatformType.jvm)
            .single() as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

        val jarTask =
            project.getTasksByName("bootJar", false).firstOrNull() as Jar?
                ?: project.getTasksByName("shadowJar", false).firstOrNull() as Jar?
                ?: project.getTasksByName("${jarTarget.name}Jar", false).single() as Jar?
                ?: throw GradleException("Jar task not found")

        val linuxTask = jarTask

        val copyBinaryToDockerTask = project.tasks.create("copyBinaryToDocker", Copy::class.java).apply {
            dependsOn(linuxTask)
            group = "build"
            from(linuxTask.archiveFile.get().asFile)
            destinationDir = File(project.buildDir, "docker")
        }

        createDockerfileTask.configure {
            it.from("bellsoft/liberica-openjdk-alpine:17.0.4-8")
            it.copyFile(linuxTask.archiveFileName.get(), "/app/binary.jar")
            it.exposePort(8080)
            it.exposePort(8081)
            it.workingDir("/app")
            it.environmentVariable("JVM_OPTS", "''")
//            it.defaultCommand("java", "\$JVM_OPTS", "-jar", "binary.jar", "-docker")
            it.defaultCommand("sh", "-c", "java \$JVM_OPTS -jar binary.jar -docker")
        }
        buildDockerBuildImageTask.configure {
            it.dependsOn(copyBinaryToDockerTask)
        }
    }
}
