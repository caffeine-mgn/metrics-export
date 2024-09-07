plugins {
//  kotlin("multiplatform") version "1.9.24" apply false
}
allprojects {
  group="pw.binom"
  repositories {
    mavenLocal()
    maven(url = "https://repo.binom.pw")
    mavenCentral()
  }
}
