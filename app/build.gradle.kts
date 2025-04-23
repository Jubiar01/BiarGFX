plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.facebooklogin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.facebooklogin"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
    buildFeatures {
        viewBinding = true // Enable View Binding
        dataBinding = true  // Enable Data Binding
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.documentfile:documentfile:1.1.0-beta01")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    implementation("com.github.rahatarmanahmed:circularprogressview:2.5.0")
}