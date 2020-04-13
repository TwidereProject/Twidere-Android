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

-dontobfuscate

-dontwarn org.codehaus.mojo.animal_sniffer.*
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
-dontwarn InnerClasses

# https://github.com/osmdroid/osmdroid/issues/633
-dontwarn org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck

# https://github.com/dropbox/dropbox-sdk-java#does-this-sdk-require-any-special-proguard-rules-for-shrink-optimizations
-dontwarn com.dropbox.core.DbxStandardSessionStore**
-dontwarn com.dropbox.core.http.OkHttpRequestor**
-dontwarn com.dropbox.core.http.GoogleAppEngineRequestor**

-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes Signature
-keepattributes InnerClasses


# https://github.com/bluelinelabs/LoganSquare
-keep class com.bluelinelabs.logansquare.annotation.JsonObject
-keep class * extends com.bluelinelabs.logansquare.JsonMapper
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *

# https://github.com/mariotaku/RestFu
-keep class org.mariotaku.restfu.annotation.** { *; }

-keep class * extends org.mariotaku.library.objectcursor.ObjectCursor$CursorIndices

# https://github.com/square/otto/
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keep class * extends android.support.v4.view.ActionProvider
-keepclassmembers class * extends android.support.v4.view.ActionProvider {
    <init>(android.content.Context);
}

# https://github.com/bumptech/glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule


# Essential components
-keep class * extends org.mariotaku.twidere.util.Analyzer
-keep class * extends org.mariotaku.twidere.util.MapFragmentFactory
-keep class * extends org.mariotaku.twidere.util.twitter.card.TwitterCardViewFactory

# Extra feature service
-keep class * extends org.mariotaku.twidere.util.premium.ExtraFeaturesService
-keep class * extends org.mariotaku.twidere.util.promotion.PromotionService

# Extra feature component factories
-keep class * extends org.mariotaku.twidere.util.gifshare.GifShareProvider$Factory
-keep class * extends org.mariotaku.twidere.util.schedule.StatusScheduleProvider$Factory
-keep class * extends org.mariotaku.twidere.util.sync.DataSyncProvider$Factory
-keep class * extends org.mariotaku.twidere.util.sync.TimelineSyncManager$Factory

# View components
-keep class * extends org.mariotaku.twidere.util.view.AppBarChildBehavior$ChildTransformation

-keepclassmembers class * {
    private <fields>;
}

-keepclassmembers class org.mariotaku.twidere.activity.BrowserSignInActivity$InjectorJavaScriptInterface {
    public *;
}