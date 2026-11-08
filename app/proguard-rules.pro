# Keep MediaSession / media metadata classes used through reflection by the platform.
-keep class android.support.v4.media.** { *; }
-keep class android.media.session.** { *; }

# Kotlin serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.motosmart.launcher.**$$serializer { *; }
-keepclassmembers class com.motosmart.launcher.** {
    *** Companion;
}
-keepclasseswithmembers class com.motosmart.launcher.** {
    kotlinx.serialization.KSerializer serializer(...);
}
