package com.ritesh.cashiro.domain.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) {
    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
        private val BANNER_IMAGE_URI_KEY = stringPreferencesKey("banner_image_uri")
        private val PROFILE_BG_COLOR_KEY = intPreferencesKey("profile_background_color")
    }

    // User Name Operations
    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    fun getUserName(): Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }

    // Profile Image URI Operations
    suspend fun saveProfileImageUri(uri: Uri?) {
        dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_URI_KEY] = uri?.toString() ?: ""
        }
    }

    fun getProfileImageUri(): Flow<Uri?> = dataStore.data.map { preferences ->
        preferences[PROFILE_IMAGE_URI_KEY]?.let {
            if (it.isNotEmpty()) {
                try {
                    Uri.parse(it)
                } catch (e: Exception) {
                    Log.e("ProfileRepository", "Invalid profile image URI: ${e.message}")
                    null
                }
            } else null
        }
    }

    // Banner Image URI Operations
    suspend fun saveBannerImageUri(uri: Uri?) {
        dataStore.edit { preferences ->
            preferences[BANNER_IMAGE_URI_KEY] = uri?.toString() ?: ""
        }
    }

    fun getBannerImageUri(): Flow<Uri?> = dataStore.data.map { preferences ->
        preferences[BANNER_IMAGE_URI_KEY]?.let {
            if (it.isNotEmpty()) {
                try {
                    Uri.parse(it)
                } catch (e: Exception) {
                    Log.e("ProfileRepository", "Invalid banner image URI: ${e.message}")
                    null
                }
            } else null
        }
    }

    // Profile Background Color Operations
    suspend fun saveProfileBackgroundColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[PROFILE_BG_COLOR_KEY] = color.toArgb()
        }
    }

    fun getProfileBackgroundColor(): Flow<Color> = dataStore.data.map { preferences ->
        val colorInt = preferences[PROFILE_BG_COLOR_KEY] ?: Macchiato_Blue.toArgb()
        Color(colorInt)
    }

    // Comprehensive Profile Data Retrieval
    fun getProfileData(): Flow<ProfileData> {
        return combine(
            getUserName(),
            getProfileImageUri(),
            getBannerImageUri(),
            getProfileBackgroundColor()
        ) { userName, profileImage, bannerImage, bgColor ->
            ProfileData(
                userName = userName,
                profileImageUri = profileImage,
                bannerImageUri = bannerImage,
                profileBackgroundColor = bgColor
            )
        }
    }

    // Validate and copy image to app-specific storage if needed
    suspend fun validateAndSaveImageUri(uri: Uri?): Uri? {
        if (uri == null) return null

        return try {
            // Attempt to open input stream to validate URI
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Create a file in app-specific storage
                val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)

                // Copy the input stream to the file
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                // Return URI of the saved file
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error saving image: ${e.message}")
            null
        }
    }

    // Data class to represent profile information
    data class ProfileData(
        val userName: String = "",
        val profileImageUri: Uri? = null,
        val bannerImageUri: Uri? = null,
        val profileBackgroundColor: Color = Macchiato_Blue
    )
}

//@Singleton
//class ProfileRepository @Inject constructor(
//    private val dataStore: DataStore<Preferences>
//) {
//    companion object {
//        private val USER_NAME_KEY = stringPreferencesKey("user_name")
//        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
//        private val BANNER_IMAGE_URI_KEY = stringPreferencesKey("banner_image_uri")
//        private val PROFILE_BG_COLOR_KEY = intPreferencesKey("profile_background_color")
//    }
//
//    // User Name Operations
//    suspend fun saveUserName(name: String) {
//        dataStore.edit { preferences ->
//            preferences[USER_NAME_KEY] = name
//        }
//    }
//
//    fun getUserName(): Flow<String> = dataStore.data.map { preferences ->
//        preferences[USER_NAME_KEY] ?: ""
//    }
//
//    // Profile Image URI Operations
//    suspend fun saveProfileImageUri(uri: Uri?) {
//        dataStore.edit { preferences ->
//            preferences[PROFILE_IMAGE_URI_KEY] = uri?.toString() ?: ""
//        }
//    }
//
//    fun getProfileImageUri(): Flow<Uri?> = dataStore.data.map { preferences ->
//        preferences[PROFILE_IMAGE_URI_KEY]?.let { Uri.parse(it) }
//    }
//
//    // Banner Image URI Operations
//    suspend fun saveBannerImageUri(uri: Uri?) {
//        dataStore.edit { preferences ->
//            preferences[BANNER_IMAGE_URI_KEY] = uri?.toString() ?: ""
//        }
//    }
//
//    fun getBannerImageUri(): Flow<Uri?> = dataStore.data.map { preferences ->
//        preferences[BANNER_IMAGE_URI_KEY]?.let { Uri.parse(it) }
//    }
//
//    // Profile Background Color Operations
//    suspend fun saveProfileBackgroundColor(color: Color) {
//        dataStore.edit { preferences ->
//            preferences[PROFILE_BG_COLOR_KEY] = color.toArgb()
//        }
//    }
//
//    fun getProfileBackgroundColor(): Flow<Color> = dataStore.data.map { preferences ->
//        val colorInt = preferences[PROFILE_BG_COLOR_KEY] ?: Macchiato_Blue.toArgb()
//        Color(colorInt)
//    }
//
//    // Comprehensive Profile Data Retrieval
//    fun getProfileData(): Flow<ProfileData> {
//        return combine(
//            getUserName(),
//            getProfileImageUri(),
//            getBannerImageUri(),
//            getProfileBackgroundColor()
//        ) { userName, profileImage, bannerImage, bgColor ->
//            ProfileData(
//                userName = userName,
//                profileImageUri = profileImage,
//                bannerImageUri = bannerImage,
//                profileBackgroundColor = bgColor
//            )
//        }
//    }
//
//    // Data class to represent profile information
//    data class ProfileData(
//        val userName: String = "",
//        val profileImageUri: Uri? = null,
//        val bannerImageUri: Uri? = null,
//        val profileBackgroundColor: Color = Macchiato_Blue
//    )
//}