plugins {
    kotlin("multiplatform") version "2.0.21"
    id("com.android.library") version "8.5.2"
    id("maven-publish")
}

group = "io.mawaqit"
version = "0.1.0"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release")
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "io.mawaqit.lib"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Mawaqit")
            description.set("High-precision Islamic prayer time calculation library for Kotlin Multiplatform")
            url.set("https://github.com/timargv/mawaqit")

            licenses {
                license {
                    name.set("GNU Affero General Public License v3.0")
                    url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                }
            }

            developers {
                developer {
                    id.set("timargv")
                    name.set("Tima")
                    email.set("tima.rgv@gmail.com")
                }
            }

            scm {
                url.set("https://github.com/timargv/mawaqit")
                connection.set("scm:git:git://github.com/timargv/mawaqit.git")
                developerConnection.set("scm:git:ssh://github.com/timargv/mawaqit.git")
            }
        }
    }
}
