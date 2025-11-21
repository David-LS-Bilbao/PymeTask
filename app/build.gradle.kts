
import org.gradle.api.tasks.testing.logging.TestLogEvent



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt) // Necesario para Hilt
    alias(libs.plugins.hilt) // Aplica el plugin de Hilt aquí
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dls.pymetask"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dls.pymetask"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }


    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }


    packaging {
        resources {
            pickFirsts.add("META-INF/LICENSE-MIT")

            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/DEPENDENCIES")

        }
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
        compose = true
        buildConfig = true      // <- ACTÍVALO

    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    testOptions{
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true // solo si usas Robolectric
        unitTests.all {
            it.testLogging {
                events = setOf(
                    TestLogEvent.FAILED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.PASSED
                )
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material3)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.ui.test.junit4)
    implementation(libs.androidx.espresso.intents)
    implementation(libs.androidx.navigation.testing)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.google.auth)
    implementation(libs.coil.compose)
    implementation(libs.reorderable)
    implementation(libs.gson)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
        // Opcional - Para integración con ViewModel de Jetpack
         implementation(libs.androidx.hilt.navigation.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Firebase BOM
    implementation(platform(libs.firebase.bom))

    // Firebase Services
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.androidx.lifecycle.viewmodel.compose) // o superior
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Retrofit
    implementation(libs.retrofit.core) // O la versión más reciente

    // Moshi
    implementation(libs.moshi) // O la versión más reciente
    implementation(libs.moshi.kotlin) // Para soporte de Kotlin (data classes, null safety)

    // Convertidor de Moshi para Retrofit
    implementation(libs.converter.moshi)
    implementation(libs.play.services.location)

    // Location.
    implementation(libs.security.crypto)
    implementation(libs.androidx.browser)

    implementation(libs.ui)
    // habilita desugaring para soportar Java 8 en minSdk 24
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.kotlinx.coroutines.core)

    // Unit tests (ViewModel y UseCases)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

// Instrumented UI tests (Compose + AndroidX Test)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
   // testImplementation(libs.kotlinx.coroutines.test.v1101)

    testImplementation(libs.truth)
//    testImplementation(libs.mockk) // por si lo usamos en siguientes tests
//    testImplementation(libs.turbine)
    androidTestImplementation (libs.mockk.mockk.android)
    testImplementation(kotlin("test"))

    testImplementation(libs.junit.jupiter) // For JUnit Jupiter (JUnit 5)

    // JUnit 5
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.jupiter.junit.jupiter.engine)

// Coroutines Test
    testImplementation(libs.kotlinx.coroutines.test)
    // MockK y Turbine
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // Instrumentación
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



}
