-keepattributes *Annotation*, InnerClasses

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-keep class com.squareup.moshi.** { *; }

-keep class com.trishop.app.data.remote.dto.** { *; }
