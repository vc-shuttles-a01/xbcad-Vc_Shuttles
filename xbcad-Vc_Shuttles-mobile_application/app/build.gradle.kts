plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //id("com.android.application")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.example.xbcad7319"
    compileSdk = 34

    viewBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.example.xbcad7319"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/NOTICE.md")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.material.v140)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation (libs.play.services.auth.v2060)

    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation(libs.firebase.analytics)

    //apply plugin: 'com.google.gms.google-services'
    implementation (libs.firebase.auth)  // Firebase Auth
    implementation (libs.firebase.firestore)  // Firestore
    implementation (libs.firebase.database)  // Realtime Database
    implementation (libs.play.services.auth)
    implementation (libs.play.services.maps) // or the version you need
    implementation (libs.firebase.database.v2000) // Adjust the version accordingly
    implementation (libs.volley) // For making network requests
    implementation (libs.android.maps.utils) // For PolyUtil

    implementation (libs.firebase.database.vlatestversion)
    implementation (libs.play.services.maps.vlatestversion)
    implementation (libs.volley.vlatestversion)

    implementation (libs.google.firebase.analytics)
    //implementation (libs.google.firebase.firestore)

    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.firebase.storage)

    implementation (libs.core)
    implementation (libs.zxing.android.embedded)

    implementation (libs.picasso)

    implementation (libs.mpandroidchart)

    implementation (libs.androidx.core.ktx.v160)

    implementation (libs.android.mail)
    implementation (libs.android.activation)

    implementation ("com.squareup.okhttp3:okhttp:4.10.0")


    //implementation (libs.play.services.basement)
}