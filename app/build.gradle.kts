val TL_SANDBOX_CLIENT_SECRET: String = (project.findProperty("TL_SANDBOX_CLIENT_SECRET") as String?) ?: ""



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

        // ✅ SANDBOX (API y Auth)
        buildConfigField("String", "BANK_BASE_URL", "\"https://api.truelayer-sandbox.com/\"") // Data API sandbox
        buildConfigField("String", "OAUTH_BASE_URL", "\"https://auth.truelayer-sandbox.com/\"") // Auth sandbox
        buildConfigField("String", "OAUTH_CLIENT_ID", "\"sandbox-pymetask-b17d46\"")
        buildConfigField("String", "OAUTH_CLIENT_SECRET", "\"${'$'}{TL_SANDBOX_CLIENT_SECRET}\"") // ← lo leeremos desde gradle.properties
        buildConfigField("String", "OAUTH_REDIRECT_URI", "\"pymetask://auth/callback\"")
        buildConfigField("String", "OAUTH_SCOPES", "\"accounts balance transactions offline_access\"")

//        buildConfigField("String", "BANK_BASE_URL", "\"https://api.truelayer.com/\"")           // Data API
//        buildConfigField("String", "OAUTH_BASE_URL", "\"https://auth.truelayer.com/\"")         // OAuth host (authorize & token)
//        buildConfigField("String", "OAUTH_CLIENT_ID", "\"sandbox-pymetask-b17d46\"")
//        buildConfigField("String", "OAUTH_CLIENT_SECRET", "\"19fa3e88-a8c4-45b9-b38b-7514be839b59\"")               // si el proveedor lo requiere
//        buildConfigField("String", "OAUTH_REDIRECT_URI", "\"pymetask://auth/callback\"")        // debe coincidir con el manifest
//        buildConfigField("String", "OAUTH_SCOPES", "\"accounts balance transactions offline_access\"")


      // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        debug {
            buildConfigField("String", "OAUTH_BASE_URL", "\"https://auth.truelayer-sandbox.com\"")
            buildConfigField("String", "OAUTH_CLIENT_ID", "\"sandbox-pymetask-b17d46\"") // <- copia exacto de Console
            buildConfigField("String", "OAUTH_REDIRECT_URI", "\"pymetask://auth/callback\"")
            // Scopes mínimos para listar cuentas y transacciones; añade balance/offline_access si quieres refresh
            buildConfigField("String", "OAUTH_SCOPES", "\"accounts transactions balance offline_access\"")
            // (Opcional) Si tu OAuthApi necesita base explícita para token:
            buildConfigField("String", "OAUTH_TOKEN_URL", "\"https://auth.truelayer-sandbox.com/connect/token\"")
        }


        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Cuando pases a producción:
            // OAUTH_BASE_URL = "https://auth.truelayer.com"
            // OAUTH_CLIENT_ID = "live-xxxx"
            // OAUTH_REDIRECT_URI igual que en Console Live
            // OAUTH_TOKEN_URL = "https://auth.truelayer.com/connect/token"

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
    testImplementation(libs.junit)
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

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)


    implementation(libs.security.crypto)
    implementation(libs.androidx.browser)

    implementation(libs.ui)
    // habilita desugaring para soportar Java 8 en minSdk 24
    coreLibraryDesugaring(libs.desugar.jdk.libs)


}
