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

# FitForm specific ProGuard rules

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep data binding classes
-keep class androidx.databinding.** { *; }

# Keep view binding classes
-keep class * extends androidx.viewbinding.ViewBinding

# Keep camera classes
-keep class androidx.camera.** { *; }

# Keep pose detection classes
-keep class com.google.mlkit.vision.pose.** { *; }

# Keep FitForm core classes
-keep class com.fit.fitform.core.** { *; }

# Keep authentication classes
-keep class com.fit.fitform.ui.auth.** { *; }

# Keep entity classes
-keep class com.fit.fitform.data.entity.** { *; }

# Keep repository classes
-keep class com.fit.fitform.data.repository.** { *; }

# Keep DAO interfaces
-keep interface com.fit.fitform.data.dao.** { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations that need generic signatures.
-if interface * { *; }
-keep,allowobfuscation interface <1>

# Keep suspend functions
-if interface * { *; }
-keep,allowobfuscation interface <1>