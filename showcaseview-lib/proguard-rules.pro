# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/hungama2/Android softwares/android-sdk-linux/tools/proguard/proguard-android.txt
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

-keepattributes InnerClasses

-keep public class com.github.amlcurran.showcaseview.ShowcaseView{
    *;
}
-keep public class com.github.amlcurran.showcaseview.ShowcaseView$Builder {
    *;
}

-keepclasseswithmembers public interface com.github.amlcurran.showcaseview.OnShowcaseEventListener{ *; }
-keepclasseswithmembers public class com.github.amlcurran.showcaseview.targets.ViewTarget{
    public <init>(android.view.View);
}
