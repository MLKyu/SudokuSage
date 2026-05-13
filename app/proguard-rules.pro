# =============================================================================
# Sudoku Sage — R8 / ProGuard rules
# AGP 9 ships R8 in full mode by default; rules below cover only what the
# bundled consumer rules from each library do not already provide.
# =============================================================================

# -----------------------------------------------------------------------------
# Stack traces — keep file/line info, rename source attribute so original
# .kt filenames are not leaked while line numbers remain mappable.
# -----------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# -----------------------------------------------------------------------------
# Kotlin
# -----------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# -----------------------------------------------------------------------------
# Coroutines (consumer rules cover most; a couple of safe dontwarns)
# -----------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.debug.**
-dontwarn kotlinx.coroutines.flow.**

# -----------------------------------------------------------------------------
# Moshi (using codegen via KSP — adapters are generated, just keep them)
# https://github.com/square/moshi#proguard
# -----------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keep,allowobfuscation,allowshrinking interface com.squareup.moshi.JsonAdapter$Factory
-keep,allowobfuscation,allowshrinking class com.squareup.moshi.JsonAdapter

-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <init>(...);
    <fields>;
}

# Keep generated *JsonAdapter classes for any @JsonClass-annotated type.
-if @com.squareup.moshi.JsonClass class *
-keep class <1>JsonAdapter {
    <init>(...);
    <fields>;
}
-if @com.squareup.moshi.JsonClass class **$*
-keep class <1>_<2>JsonAdapter {
    <init>(...);
    <fields>;
}

-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# -----------------------------------------------------------------------------
# Retrofit
# https://square.github.io/retrofit/#download (R8 / ProGuard section)
# -----------------------------------------------------------------------------
-keepattributes Exceptions

-keepclasseswithmembers,includedescriptorclasses class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**

# Retrofit suspend bridge uses Kotlin's Continuation; keep its signature.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Platform classes Retrofit looks up reflectively but does not actually require.
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.bouncycastle.**

# -----------------------------------------------------------------------------
# OkHttp / Okio
# -----------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**

# -----------------------------------------------------------------------------
# Koin (constructor DSL — no reflection-based keeps required, just silence
# warnings for optional ksp / annotation paths the app doesn't use)
# -----------------------------------------------------------------------------
-dontwarn org.koin.**

# -----------------------------------------------------------------------------
# Play Services Location (consumer rules cover most; safety dontwarn)
# -----------------------------------------------------------------------------
-dontwarn com.google.android.gms.**

# -----------------------------------------------------------------------------
# Material Components / AndroidX (consumer rules suffice; nothing extra here)
# Compose, Room, DataStore, CameraX, Coil all ship their own consumer rules.
# -----------------------------------------------------------------------------
