package org.tlsys

import org.gradle.api.Project

internal fun runExternalProgram(vararg arg: String): String = runExternalProgram(arg.toList())
internal fun runExternalProgram(args: List<String>): String {
    val process = ProcessBuilder()
        .command(args)
        .redirectErrorStream(true)
        .start()
    val result = StringBuilder()
    val reader = process.inputStream.bufferedReader()
    while (process.isAlive) {
        result.append(reader.readText())
    }
    return result.toString()
}

val Project.imageName: String?
    get() {
        val dest = description ?: return null
        val sb = StringBuilder()
        description!!.forEach {
            if (it.isUpperCase()) {
                sb.append("-").append(it.toLowerCase())
            } else {
                sb.append(it)
            }
        }
        return "tl-${sb.toString().removePrefix("-")}"
    }
