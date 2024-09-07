package org.tlsys.helm

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URL

abstract class HelmUpload : DefaultTask() {
    @get:InputFile
    abstract val file: RegularFileProperty

    init {
        group = "helm"
    }

    @TaskAction
    fun execute() {
        val f = file.get().asFile
        val url = "${System.getenv("HELM_REPOSITORY")}/api/charts?force"
        logger.lifecycle("URL for upload: $url")
        logger.lifecycle("File: $f")
//        val url = "http://127.0.0.1:13370/api/charts"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.addRequestProperty("Content-Length", f.length().toULong().toString())
        connection.doOutput = true
        connection.connectTimeout = 2000
        connection.readTimeout = 10000

        f.inputStream().use { file ->
            connection.outputStream.use { output ->
                file.copyTo(output)
                output.flush()
            }
        }
        if (connection.responseCode != 200 && connection.responseCode != 201 && connection.responseCode != 202) {
            val errorMsg = connection.errorStream.bufferedReader().use { it.readText() }
            throw GradleException("Can't upload helm. Invalid response: ${connection.responseCode}: $errorMsg")
        }
    }
}
