# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\vaas\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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

-dontwarn com.dropbox.**
-dontwarn org.junit.**
-dontwarn org.jasypt.**
-dontwarn android.test.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn com.github.omadahealth.**
-keep class ir.the_moment.carmichael_sms.tasks.Task
-keep class * extends ir.the_moment.carmichael_sms.tasks.Task
-keep class ir.the_moment.carmichael_sms.tasks.UserActivatedTask
-keep class com.dropbox.core.**
-keep class ir.the_moment.carmichael_sms.responseHandler.ResponseHandler
-keep class * extends ir.the_moment.carmichael_sms.responseHandler.ResponseHandler
-keep class * extends ir.the_moment.carmichael_sms.tasks.UserActivatedTask
-keep class * extends ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keepnames class com.fasterxml.jackson.** {
*;
}
-keepnames interface com.fasterxml.jackson.** {
    *;
}