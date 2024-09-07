package org.tlsys.helm

import org.gradle.api.tasks.OutputFile
import org.gradle.process.CommandLineArgumentProvider

abstract class HelmPackage : HelmExec() {
    @OutputFile
    val resultFile = helmProject.map { outputHelmDir.resolve("$it-$version.tgz") }

    init {
        commandLine.set(listOf("helm"))
        workingDir.fileProvider(helmDirectory)
        argumentProviders.add(CommandLineArgumentProvider {
            listOf(
                "package",
                directory.get().asFile.absolutePath,
                "--app-version",
                version,
                "--version",
                version,
                "--dependency-update",
                "--destination",
                outputHelmDir.absolutePath,
            )
        })
        standardOutput = System.out
        errorOutput = System.out
        group = "helm"
        outputs.file(resultFile)
    }
}