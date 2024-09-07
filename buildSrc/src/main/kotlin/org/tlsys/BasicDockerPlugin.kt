package org.tlsys

import com.bmuschko.gradle.docker.tasks.AbstractDockerRemoteApiTask
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.DockerRemoveImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.io.File
import java.util.*

const val DOCKER_PUBLISH_TASK = "pushDockerImage"

internal val dockerBuildProjects = ArrayList<Project>()

@OptIn(kotlin.ExperimentalStdlibApi::class)
abstract class BasicDockerPlugin : Plugin<Project> {
    fun Project.findKotlin() = project.extensions.findByName("kotlin") as KotlinMultiplatformExtension
    fun KotlinMultiplatformExtension.findTarget(type: KotlinPlatformType) = targets.filter { it.platformType == type }

    lateinit var description: String
        private set
    lateinit var imageName: String
        private set

    lateinit var publicDockerTask: TaskProvider<DockerPushImage>
        private set
    lateinit var buildDockerBuildImageTask: TaskProvider<DockerBuildImage>
        private set
    lateinit var createDockerfileTask: TaskProvider<Dockerfile>

    override fun apply(project: Project) {

        project.plugins.apply(com.bmuschko.gradle.docker.DockerRemoteApiPlugin::class.java)
        val e = project.extensions.getByType(com.bmuschko.gradle.docker.DockerExtension::class.java)
        e.registryCredentials.url.set("https://images.binom.pw/")
        e.registryCredentials.username.set(System.getenv("DOCKER_REGISTRY_USERNAME"))
        e.registryCredentials.password.set(System.getenv("DOCKER_REGISTRY_PASSWORD"))

        println("Login: ${System.getenv("DOCKER_REGISTRY_USERNAME")}")
        println("Password: ${System.getenv("DOCKER_REGISTRY_PASSWORD")}")

        description = project.imageName ?: TODO("Description not set")
        imageName = "images.binom.pw/${description.lowercase()}:${project.version}"
        val imageName2 = "images.binom.pw/${description.lowercase()}:latest"

        createDockerfileTask = project.tasks.register("createDockerfile", Dockerfile::class.java)
        createDockerfileTask.configure {
            it.group = "build"
            it.destFile.set(File(project.buildDir, "docker/Dockerfile"))
        }
        buildDockerBuildImageTask = project.tasks.register("buildDockerBuildImage", DockerBuildImage::class.java)
        buildDockerBuildImageTask.configure {
            it.applyUrl()
            it.group = "build"
            it.dependsOn(createDockerfileTask)
            it.inputDir.set(
                project.layout.dir(
                    createDockerfileTask.map {
                        it.destFile.get().asFile.parentFile!!
                    }
                )
            )
            it.images.add(imageName)
            it.images.add(imageName2)
        }
        publicDockerTask = project.tasks.register(DOCKER_PUBLISH_TASK, DockerPushImage::class.java)
        publicDockerTask.configure {
            it.applyUrl()
            it.group = "publishing"
            it.dependsOn(buildDockerBuildImageTask)
            it.images.add(imageName)
            it.images.add(imageName2)
        }
        project.tasks.register("removeDockerImage", DockerRemoveImage::class.java)
            .configure {
                it.mustRunAfter(publicDockerTask)
                it.dependsOn(buildDockerBuildImageTask)
                it.imageId.set(imageName)
                it.imageId.set(imageName2)
            }
        dockerBuildProjects += project
    }
}

fun AbstractDockerRemoteApiTask.applyUrl() {
    val host = System.getenv("DOCKER_HOST")
    if (host != null) {
        url.set(host)
    }
}