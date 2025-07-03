# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep generic signature of TypeToken and its subclasses
-keepattributes Signature

# Preserve types used by Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.** { *; }

# Keep your model classes used with Gson
-keep class com.ritesh.cashiro.data.model.** { *; }
-keep class com.ritesh.cashiro.domain.model.** { *; }

# Keep DataStore classes
-keep class androidx.datastore.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class * implements androidx.datastore.preferences.core.Preferences { *; }

# Keep any custom serializers
-keep class * implements androidx.datastore.core.Serializer { *; }

# Keep all entity classes for Gson serialization
-keep class com.ritesh.cashiro.data.model.** { *; }
-keep class com.ritesh.cashiro.domain.model.** { *; }

# Keep classes with @Keep annotation
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep entity classes specifically
-keep class **.AccountEntity { *; }
-keep class **.CategoryEntity { *; }
-keep class **.SubCategoryEntity { *; }
-keep class **.TransactionEntity { *; }
-keep class **.AppBackupData { *; }
-keep class **.Recurrence { *; }
-keep enum **.TransactionType { *; }
-keep enum **.RecurrenceFrequency { *; }

# Room specific rules (in case they help)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
