# JomaKhoroch (জমাখরচ) — Production ProGuard / R8 Rules

# Keep Room database, entities, and generated DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Keep domain accounting models and enum names used in Room/backup serialization
-keep class com.example.domain.accounting.** { *; }
-keep class com.example.core.localization.** { *; }
-keep class com.example.ui.theme.ThemeMode { *; }
-keep class com.example.infrastructure.BackupPreview { *; }
-keep class com.example.infrastructure.BackupExportResult { *; }

# Keep Firebase Firestore cloud sync models
-keep @androidx.annotation.Keep class * { *; }
-keep class com.example.data.cloud.** { *; }

# Keep Jetpack DataStore and Coroutines
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Google Play Services & Credential Manager for Google Sign-In
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# Preserve source line numbers for clean crash diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
