package org.tlsys

private val gitBranch by lazy {
    runExternalProgram("git", "rev-parse", "--abbrev-ref", "HEAD").trim()
}
private val gitHash by lazy {
    runExternalProgram("git", "rev-parse", "--short", "HEAD").trim()
}
private val gitCommitCount by lazy {
    runExternalProgram("git", "rev-list", "--count", "HEAD").trim().toLong()
}

@JvmName("getGitBranch2")
fun getGitBranch(): String = gitBranch

@JvmName("getGitHash2")
fun getGitHash(): String = gitHash

@JvmName("getGitCommitCount2")
fun getGitCommitCount(): Long = gitCommitCount
