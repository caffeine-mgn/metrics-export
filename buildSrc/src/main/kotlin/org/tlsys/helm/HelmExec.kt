package org.tlsys.helm

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.CommandLineArgumentProvider
import java.io.OutputStream

abstract class HelmExec : DefaultTask() /*: Exec()*/ {
    @get:InputDirectory
    abstract val directory: RegularFileProperty

    @get:Internal
    protected val helmDirectory = directory.map { it.asFile.parentFile }

    @get:Input
    abstract val helmProject: Property<String>

    @get:Input
    abstract val commandLine: ListProperty<String>

    @get:Internal
    val argumentProviders = ArrayList<CommandLineArgumentProvider>()

    @get:InputDirectory
    abstract val workingDir: RegularFileProperty

    @get:Internal
    protected var standardOutput: OutputStream = System.out

    @get:Internal
    protected var errorOutput: OutputStream = System.err

    @get:Internal
    protected val outputHelmDir = project.buildDir.resolve("helm")

    @get:Internal
    protected val version
        get() = project.version.toString().let { if (it == "dev") "0.0.1" else it }

    @TaskAction
    fun execute() {
        val e = (commandLine.get() + argumentProviders.map { it.asArguments().toList() }.flatten().map { "\"$it\"" }).joinToString()
        logger.lifecycle("Run $e")
        project.exec {
            it.workingDir = workingDir.get().asFile
            it.commandLine(commandLine.get())
            it.standardOutput = standardOutput
            it.errorOutput = errorOutput
            it.argumentProviders.addAll(argumentProviders)
        }
    }
}
