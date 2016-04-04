# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/mariotaku/Tools/android-sdk/tools/proguard/proguard-android.txt
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

#-dontobfuscate

-dontwarn com.squareup.haha.**
-dontwarn com.makeramen.roundedimageview.**
-dontwarn jnamed**
-dontwarn org.xbill.DNS.**
-dontwarn com.bluelinelabs.logansquare.**
-dontwarn okio.**
-dontwarn android.support.**
-dontwarn com.afollestad.**
-dontwarn com.facebook.stetho.**
-dontwarn com.google.android.**
-dontwarn okhttp3.**
-dontwarn sun.net.spi.**
-dontwarn sun.misc.**
-dontwarn sun.nio.**
-dontwarn java.nio.file.**


-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes Signature
-keepattributes InnetClasses

# https://github.com/bluelinelabs/LoganSquare
-keep class com.bluelinelabs.logansquare.annotation.JsonObject
-keep class * extends com.bluelinelabs.logansquare.JsonMapper
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *

-keep class org.mariotaku.twidere.api.twitter.annotation.NoObfuscate
-keep @org.mariotaku.twidere.api.twitter.annotation.NoObfuscate class *

# https://github.com/mariotaku/RestFu
-keep class org.mariotaku.restfu.annotation.** { *; }

# http://square.github.io/otto/
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keep class * extends android.support.v4.view.ActionProvider
-keepclassmembers class * extends android.support.v4.view.ActionProvider {
    <init>(android.content.Context);
}

-keepclassmembers class * {
    private <fields>;
}

-keepclassmembers class org.mariotaku.twidere.activity.BrowserSignInActivity$InjectorJavaScriptInterface {
    public *;
}