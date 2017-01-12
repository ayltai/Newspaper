# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/alan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,Signature,*Annotation*

### Android Support Library
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

### Android Design Support Library
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

### Android Support Library v7 (AppCompat)
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

### Android Support Library v7 (CardView)
# http://stackoverflow.com/questions/29679177/cardview-shadow-not-appearing-in-lollipop-after-obfuscate-with-proguard/29698051
-keep class android.support.v7.widget.RoundRectDrawable { *; }

### OkHttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**

### Realm
-dontnote io.realm.**

### Google Firebase
-keep class com.firebase.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.** { *; }

### Google Play Services
-keep class com.google.android.gms.common.api.GoogleApiClient {
    void connect();
    void disconnect();
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
	@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
	public static final ** CREATOR;
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

### Fabric
-keep class io.fabric.**
-keep interface io.fabric.**
-dontwarn io.fabric.**

### Appsee
-keep class com.appsee.** { *; }
-dontwarn com.appsee.**

### Yahoo Flurry
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepclasseswithmembers class * {
   public <init>(android.content.Context, android.util.AttributeSet, int);
}

### Facebook Fresco
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

-keep class com.facebook.** { *; }
-dontnote com.facebook.**

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontnote okio.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**

### PhotoDraweeView
-keep class me.relex.photodraweeview.** { *; }

### MaterialStyledDialogs
-keep class me.zhanghai.android.materialprogressbar.** { *; }
-keep class com.afollestad.materialdialogs.** { *; }

### SmallBang
-keep class xyz.hanks.library.** { *; }

### Retrolambda
-dontwarn java.lang.invoke.*

### Apache Legacy HTTP Client
-keep public class org.apache.http.**
-keepclassmembers public class org.apache.http.** { *; }
-dontnote org.apache.http.**

### RxJava
-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.invoke.*
-keep class rx.internal.util.unsafe.** { *; }
-dontwarn rx.internal.util.unsafe.**
-dontnote rx.internal.util.unsafe.**
-keep class rx.Scheduler { *; }
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

# LeakCanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
