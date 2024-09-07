@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.tlsys.helm

import org.gradle.api.Plugin
import org.gradle.api.Project

open class HelmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val helmDir = project.buildFile.parentFile.resolve("helm")
        val stageHelm = helmDir.resolve("stage")
        val tlHelm = helmDir.resolve("yandex")
//        val stageDependencyUpdate = project.tasks.register("stageDependencyUpdate", HelmDependencyUpdate::class.java) {
//            it.directory.set(stageHelm)
//            it.config()
//        }
        val version =
            if (project.version == "dev" || project.version == "unspecified") "0.0.1" else project.version.toString()
//        val tlUpdate =
//            project.tasks.register("tlUpdateVersion", ChartUpdateTask::class.java) {
//                it.directory.set(tlHelm)
//                it.change {
//                    it.version = version
//                    it.appVersion = version
//                }
//            }
//        val stageUpdate =
//            project.tasks.register("stageUpdateVersion", ChartUpdateTask::class.java) {
//                it.directory.set(stageHelm)
//                it.change {
//                    it.version = version
//                    it.appVersion = version
//                    it.dependencies.forEach {
//                        if (it.name == "tl") {
//                            it.version = version
//                        }
//                    }
//                }
//                it.mustRunAfter(tlUpdate)
//            }
        val stagePackage =
            project.tasks.register("stagePackage", HelmPackage::class.java) {
                it.group = "helm"
//                it.dependsOn(stageUpdate)
                it.directory.set(stageHelm)
                it.helmProject.set("tl-stage")
            }
        val stageUpload =
            project.tasks.register("stageUpload", HelmUpload::class.java) {
                it.dependsOn(stagePackage)
                it.file.set(stagePackage.get().resultFile.get())
            }
        val tlPackage =
            project.tasks.register("tlPackage", HelmPackage::class.java) {
                it.directory.set(tlHelm)
                it.helmProject.set("yandex")
            }
        val helmUploadTask =
            project.tasks.register("helmUpload", HelmUpload::class.java) {
                it.dependsOn(tlPackage)
                it.file.set(tlPackage.get().resultFile.get())
            }
        project.tasks.register("publishHelm") {
            it.group = "publishing"
            it.dependsOn(stageUpload)
            it.dependsOn(helmUploadTask)
        }
    }
}
