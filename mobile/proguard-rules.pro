-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembernames enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-dontwarn java.lang.invoke.*
-dontwarn javax.**

## Facebook Fresco

# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**

## Realm
-dontwarn io.realm.**

## Retrofit
-dontwarn retrofit2.**

## SimpleXML
-dontwarn org.simpleframework.xml.stream.**
-keep class org.simpleframework.xml.** { *; }
-keepclassmembers,allowobfuscation class * {
    @org.simpleframework.xml.* <fields>;
    public <init>();
}

## TextRazor
-keep class com.textrazor.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembernames public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
    public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *;
}

## SearchView
-keep class android.support.v7.widget.SearchView { *; }

## BottomNavigationView
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
    boolean mShiftingMode;
}
