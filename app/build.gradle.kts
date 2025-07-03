import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.ritesh.cashiro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ritesh.cashiro"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"
        setProperty("archivesBaseName", "Cashiro v$versionName")
        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")

    try {
        val keystoreProperties = Properties()
        FileInputStream(keystorePropertiesFile).use { inputStream ->
            keystoreProperties.load(inputStream)
        }

        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    } catch (ignored: Exception) {
        logger.warn("Could not load signing config: ${ignored.message}")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = true
            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
            applicationIdSuffix = ".debug"
            resValue("string", "derived_app_name", "Cashiro (Debug)")
            signingConfig = releaseSigning
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
            resValue("string", "derived_app_name", "Cashiro")
            signingConfig = releaseSigning
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

    flavorDimensions += "distribution"

    productFlavors {
        create("standard") {
            isDefault = true
            dimension = "distribution"
            resValue("string", "derived_app_name", "Cashiro")
        }

        create("foss") {
            dimension = "distribution"
            applicationIdSuffix = ".foss"
            resValue("string", "derived_app_name", "Cashiro (FOSS)")
        }
    }

    sourceSets {
        getByName("standard") {
            java.srcDirs("src/standard/java")
        }

        getByName("foss") {
            java.srcDirs("src/foss/java")
        }
    }

    if (hasProperty("splitApks")) {
        splits {
            abi {
                isEnable = true
                reset()
                include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
                isUniversalApk = false
            }
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        jniLibs.excludes += setOf(
            "/META-INF/*",
            "/META-INF/versions/**",
            "/org/bouncycastle/**",
            "/kotlin/**",
            "/kotlinx/**"
        )

        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt",
            "META-INF/MANIFEST.MF",
            "META-INF/*.RSA",
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/maven/**",
            "META-INF/proguard/**",
            "/*.properties",
            "rebel.xml"
        )

        jniLibs.useLegacyPackaging = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Configure KSP options for Hilt
ksp {
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
    arg("dagger.fastInit", "enabled")
    arg("dagger.hilt.android.internal.projectType", "APP")
    arg("dagger.hilt.internal.useAggregatingRootProcessor", "false")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.animation)

    // Room Dependency
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

    // Hilt dependency
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Skydoves color picker
    implementation(libs.compose.colorpicker)

    // Preference DataStore
    implementation(libs.androidx.datastore.preferences)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.mathparser.org.mxparser)

    // Accompanist for Navigation animation
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.navigation.material)

    // Compose Charts
    implementation(libs.compose.charts)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.gif)

    // Retrofit for API calls (duplicate entries removed)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Blur Effects
    implementation(libs.chrisbanes.haze)

    // Drag and Drop Reordering
    implementation(libs.reorderable)

    // Backup and restore
    implementation(libs.androidx.activity.compose.v182)
    implementation(libs.androidx.documentfile)

    implementation(libs.kotlin.metadata.jvm)

    implementation ("androidx.glance:glance-appwidget:1.1.1")
    implementation ("androidx.glance:glance-material3:1.1.1")
    implementation ("androidx.glance:glance-material:1.1.1")
}

//import java.io.FileInputStream
//import java.util.Properties
//
//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
//    id("com.google.devtools.ksp")
//    id("com.google.dagger.hilt.android")
//}
//
//android {
//    namespace = "com.ritesh.cashiro"
//    compileSdk = 35
//
//    defaultConfig {
//        applicationId = "com.ritesh.cashiro"
//        minSdk = 29
//        targetSdk = 35
//        versionCode = 2
//        versionName = "1.0.0"
//        setProperty("archivesBaseName", "Cashiro v$versionName")
//        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//    val keystorePropertiesFile = rootProject.file("keystore.properties")
//    var releaseSigning = signingConfigs.getByName("debug")
//
//    try {
//        val keystoreProperties = Properties()
//        FileInputStream(keystorePropertiesFile).use { inputStream ->
//            keystoreProperties.load(inputStream)
//        }
//
//        releaseSigning = signingConfigs.create("release") {
//            keyAlias = keystoreProperties.getProperty("keyAlias")
//            keyPassword = keystoreProperties.getProperty("keyPassword")
//            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
//            storePassword = keystoreProperties.getProperty("storePassword")
//        }
//    } catch (ignored: Exception) {
//        logger.warn("Could not load signing config: ${ignored.message}")
//    }
//
//    buildTypes {
//        debug {
//            isMinifyEnabled = false
//            isShrinkResources = false
//            isCrunchPngs = true
//            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
//            applicationIdSuffix = ".debug"
//            resValue("string", "derived_app_name", "Cashiro (Debug)")
//            signingConfig = releaseSigning
//        }
//        release {
//            isMinifyEnabled = true
//            isShrinkResources = true
//            isCrunchPngs = true
//            proguardFiles("proguard-android-optimize.txt", "proguard.pro", "proguard-rules.pro")
//            resValue("string", "derived_app_name", "Cashiro")
//            signingConfig = releaseSigning
//        }
//    }
//
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//    kotlinOptions {
//        jvmTarget = "17"
//    }
//    buildFeatures {
//        compose = true
//        buildConfig = true
//    }
//    flavorDimensions += "distribution"
//
//
//    productFlavors {
//        create("standard") {
//            isDefault = true
//            dimension = "distribution"
//            resValue("string", "derived_app_name", "Cashiro")
//        }
//
//        create("foss") {
//            dimension = "distribution"
//            applicationIdSuffix = ".foss"
//            resValue("string", "derived_app_name", "Cashiro (FOSS)")
//        }
//    }
//
//    sourceSets {
//        getByName("standard") {
//            java.srcDirs("src/standard/java")
//        }
//
//        getByName("foss") {
//            java.srcDirs("src/foss/java")
//        }
//    }
//
//    if (hasProperty("splitApks")) {
//        splits {
//            abi {
//                isEnable = true
//                reset()
//                include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
//                isUniversalApk = false
//            }
//        }
//    }
//
//    dependenciesInfo {
//        includeInApk = false
//        includeInBundle = false
//    }
//
//    packaging {
//        jniLibs.excludes += setOf(
//            "/META-INF/*",
//            "/META-INF/versions/**",
//            "/org/bouncycastle/**",
//            "/kotlin/**",
//            "/kotlinx/**"
//        )
//
//        // Fix for duplicate META-INF files from Apache HTTP components
//        resources.excludes += setOf(
//            "META-INF/DEPENDENCIES",
//            "META-INF/LICENSE",
//            "META-INF/LICENSE.txt",
//            "META-INF/NOTICE",
//            "META-INF/NOTICE.txt",
//            "META-INF/MANIFEST.MF",
//            "META-INF/*.RSA",
//            "META-INF/*.SF",
//            "META-INF/*.DSA",
//            "META-INF/maven/**",
//            "META-INF/proguard/**",
//            "/*.properties",
//            "rebel.xml"
//        )
//
//        jniLibs.useLegacyPackaging = true
//    }
//
//    lint {
//        abortOnError = false
//        checkReleaseBuilds = false
//    }
//
//
//}
//
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//    implementation(libs.androidx.navigation.compose)
//    implementation(libs.androidx.foundation.layout.android)
//    implementation (libs.androidx.animation)
//
//    // Room Dependency
//    implementation(libs.androidx.room.ktx)
//    implementation(libs.androidx.room.runtime)
//    annotationProcessor(libs.androidx.room.compiler)
//    ksp(libs.androidx.room.compiler)
//
//
//    // hilt dependency
//    implementation(libs.hilt.android)
//    implementation(libs.androidx.hilt.navigation.compose)
//    ksp(libs.hilt.android.compiler)
//
//
//    // skydoves color picker
//    implementation(libs.compose.colorpicker)
//
//    // Preference DataStore
//    implementation(libs.androidx.datastore.preferences)
//    // Networking
//    implementation(libs.retrofit)
//    implementation(libs.converter.gson)
//    implementation(libs.kotlinx.coroutines.android)
//
//    implementation (libs.mathparser.org.mxparser)
//
//    // Accompanist for Navigation animation
//    implementation (libs.accompanist.systemuicontroller)
//    implementation (libs.accompanist.navigation.material)
//
//    // Compose Charts
//    implementation (libs.compose.charts)
//
//    // Coil
//    implementation(libs.coil.compose)
//    implementation(libs.coil.network.okhttp)
//    implementation(libs.coil.gif)
//
//
//    // Retrofit for API calls
//    implementation(libs.retrofit)
//    implementation(libs.converter.gson)
//    implementation(libs.okhttp)
//    implementation(libs.logging.interceptor)
//
//    // Coroutines
//    implementation(libs.kotlinx.coroutines.core)
//    implementation(libs.kotlinx.coroutines.android)
//
//    // Splash Screen
//    implementation (libs.androidx.core.splashscreen)
//
//    // Blur Effects
//    implementation(libs.chrisbanes.haze)
//    // Drag and Drop Reordering
//    implementation(libs.reorderable)
//
//    // Backup and restore
//    implementation (libs.androidx.activity.compose.v182)
//    implementation (libs.androidx.documentfile)
//}
