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

-dontwarn sun.net.spi.**
-dontwarn java.nio.file.**
-dontwarn org.codehaus.mojo.**

# https://github.com/bluelinelabs/LoganSquare
-keep class com.bluelinelabs.logansquare.** { *; }
-keep @com.bluelinelabs.logansquare.annotation.JsonObject class *
-keep class **$$JsonObjectMapper { *; }

# http://square.github.io/otto/
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keepclassmembers class android.support.v7.internal.app.WindowDecorActionBar {
    private android.support.v7.internal.widget.ActionBarContextView mContextView;
    private android.support.v7.internal.widget.DecorToolbar mDecorToolbar;
}
-keepclassmembers class android.support.v7.internal.widget.ActionBarOverlayLayout {
    private android.graphics.drawable.Drawable mWindowContentOverlay;
}