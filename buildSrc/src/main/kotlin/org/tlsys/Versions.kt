package org.tlsys

object Versions {
    const val KOTLIN_VERSION = BuildConfig.KOTLIN_VERSION
    const val KOTLINX_COROUTINES_VERSION = BuildConfig.KOTLINX_COROUTINES_VERSION
    const val KOTLINX_SERIALIZATION_VERSION = BuildConfig.KOTLINX_SERIALIZATION_VERSION

    val TL_VERSION: String
        get() {
            val mainName = if (getGitBranch() == "master" || getGitBranch() == "main") {
                "release"
            } else {
                "develop"
            }
            val secondName = System.getenv("DRONE_TAG")?:BUILD_NUM
            if (secondName==null){
                return "dev"
            }
            return "$mainName-$secondName"
//            if (getGitBranch() == "master" || getGitBranch() == "main") {
//                val tag = System.getenv("DRONE_TAG")
//                if (tag != null) {
//                    return "release-$tag"
//                }
//            }
//            if (BUILD_NUM != null) {
//                return "develop-$BUILD_NUM"
//            }
//            return "dev"
        }
    val BUILD_NUM: String?
        get() = System.getenv("BUILD_NUMBER")
            ?: System.getenv("GO_TO_REVISION")
            ?: System.getenv("DRONE_BUILD_NUMBER")
}
