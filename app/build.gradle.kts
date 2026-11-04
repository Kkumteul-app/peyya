plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.peyya.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.peyya.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.pleos.navi.helper)
    implementation(libs.pleos.vehicle)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

// Windows + non-ASCII project path: Gradle Test worker can fail to load
// classes from directories under the Korean workspace path (CNFE).
// Remap unit-test outputs to an ASCII temp dir before the worker starts.
afterEvaluate {
    tasks.named<Test>("testDebugUnitTest").configure {
        val asciiClassesDir = file("${System.getProperty("java.io.tmpdir")}/peyya-debugUnitTest-classes")
        doFirst {
            asciiClassesDir.deleteRecursively()
            asciiClassesDir.mkdirs()
            val kotlinOut = layout.buildDirectory.dir("tmp/kotlin-classes/debugUnitTest").get().asFile
            if (kotlinOut.exists()) {
                kotlinOut.copyRecursively(asciiClassesDir, overwrite = true)
            }
            val javacOut = layout.buildDirectory
                .dir("intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes")
                .get()
                .asFile
            if (javacOut.exists()) {
                javacOut.copyRecursively(asciiClassesDir, overwrite = true)
            }
            classpath = files(asciiClassesDir) + classpath.filter { entry ->
                val normalized = entry.absolutePath.replace('\\', '/')
                !normalized.contains("/kotlin-classes/debugUnitTest") &&
                    !normalized.contains("/javac/debugUnitTest/")
            }
        }
    }
}
