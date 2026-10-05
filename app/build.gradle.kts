plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
	id("com.google.devtools.ksp")
}

android {
	namespace = "com.alfonsokitoko.gestionpedidos"
	compileSdk {
		version = release(36) {
			minorApiLevel = 1
		}
	}

	defaultConfig {
		applicationId = "com.alfonsokitoko.gestionpedidos"
		minSdk = 26
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	// ++ EXCEL / DOCUMENTOS (Apache POI) ++
	implementation(libs.poi)
	implementation(libs.poi.scratchpad)

	// ++ RED & JSON (Moshi & Retrofit) ++
	implementation(libs.retrofit)
	implementation(libs.moshi.kotlin)
	implementation(libs.retrofit.converter.moshi)
	implementation(libs.logging.interceptor)

	// ++ ROOM ++
	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)

	// ++ JETPACK COMPOSE & UI ++
	implementation(libs.androidx.material3)
	implementation(libs.androidx.compose.material3.v130)
	implementation(libs.androidx.compose.material.icons.extended)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.ui.text)

	// ++ CORE & LIFECYCLE ++
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.work.runtime.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)

	// ++ TESTING ++
	testImplementation(libs.junit)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.mockito.core)
	testImplementation(libs.mockito.kotlin)
	testImplementation(libs.androidx.arch.core.testing)

	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)

	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}