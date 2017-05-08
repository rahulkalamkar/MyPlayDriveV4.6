# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/hungama2/Android softwares/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-optimizationpasses 5
-dontusemixedcaseclassnames
-keepattributes SourceFile,LineNumberTable

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}


-dontwarn com.gigya.**
-keep class com.gigya.** { *; }

-dontwarn android.webkit.WebView
-dontwarn org.json.**

-dontwarn com.facebook.**
-keep class com.facebook.** { *; }

-dontwarn com.flurry.**
-keep class com.flurry.** { *; }

-dontwarn com.urbanairship.**
#-keep class com.urbanairship.** { *; }

-dontwarn com.squareup.**
#-keep class com.squareup.** { *; }

-dontwarn okio.**
#-keep class okio.** { *; }

# For Gson
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers enum * { *; }


# added due to java.lang.NoClassDefFoundError: android.support.v7.internal.view.menu.MenuBuilder issue
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn com.google.android.gms.**
#-keep class com.google.android.gms.** { *; }

-dontwarn com.reverie.lm.**
#-keep class com.reverie.lm.** { *; }

-dontwarn android.support.v8.renderscript.**
-keep class android.support.v8.renderscript.** { *; }

-dontwarn com.hungama.myplay.activity.**
-keep public class com.hungama.myplay.activity.** { *; }
#-keep public class * extends android.support.v4.app.DialogFragment

#To maintain custom components names that are used on layouts XML:
#-keep public class * extends com.hungama.myplay.activity.ui.widgets.** {
#    public <init>(android.content.Context);
#}
#-keep public class * extends com.hungama.myplay.activity.ui.widgets.** {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
#-keep public class * extends com.hungama.myplay.activity.ui.widgets.** {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}

#-dontwarn com.google.android.exoplayer.**