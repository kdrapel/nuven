# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep kotlinx.serialization annotations
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# kotlinx-serialization-json specific
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Serializers
-keep,includedescriptorclasses class com.kdr.nuven.**$$serializer { *; }
-keepclassmembers class com.kdr.nuven.** {
    *** Companion;
}
-keepclasseswithmembers class com.kdr.nuven.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class com.kdr.nuven.TeletextData { *; }
-keep class com.kdr.nuven.Headline { *; }
-keep class com.kdr.nuven.Page { *; }
-keep class com.kdr.nuven.Layout { *; }
-keep class com.kdr.nuven.Header { *; }
-keep class com.kdr.nuven.Title { *; }
-keep class com.kdr.nuven.Entry { *; }
