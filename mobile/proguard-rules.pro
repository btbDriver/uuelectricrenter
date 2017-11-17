# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\android-sdk-windows/tools/proguard/proguard-android.txt
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

# Glide图片库的混淆处理
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 高德地图混淆脚本
-keep class com.android.support.**{ *; }
-keep interface android.support.v4.app.**{ *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-dontwarn com.amap.api.**
-dontwarn com.a.a.**
-dontwarn com.autonavi.**
-keep class com.amap.api.** {*;}
-keep class com.autonavi.** {*;}
-keep class com.a.a.** {*;}

# Gson混淆脚本
-keep class com.google.gson.stream.** {*;}
-keep class com.youyou.uuelectric.renter.Network.user.** {*;}

# butterknife混淆脚本
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

# -------------系统类不需要混淆 --------------------------
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.**
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * { # 保持native方法不被混淆
    native <methods>;
}
-keepclasseswithmembernames class * { # 保持自定义控件不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * { # 保持自定义控件不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * { # 保持枚举enum类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable { # 保持Parcelable不被混淆
  public static final android.os.Parcelable$Creator *;
}

# --------- 忽略异常提示 --------------------
-dontwarn butterknife.internal.**
-dontwarn com.alipay.**
-dontwarn com.mikepenz.**
-dontwarn org.apache.**
-dontwarn com.amap.**
-dontwarn com.android.volley.**
-dontwarn com.rey.**
-dontwarn com.testin.**
-dontwarn jp.wasabeef.**

# ---------- 保持代码 --------------
-keep class com.youyou.uuelectric.renter.Utils.** {*;}
-keep class it.neokree.** {*;}
-keep class org.apache.** {*;}
-keep class com.iflytek.** {*;}
-keep class com.google.protobuf.** { *; }
-keep class com.youyou.uuelectric.renter.pay.** {*;}

# ---------------- eventbus避免混淆 ------------
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}


# --------------- 友盟统计避免混淆 -------------------------
-dontwarn android.support.v4.**
-dontwarn org.apache.commons.net.**
-dontwarn com.tencent.**
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**
-keep class com.facebook.**
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.openapi.** implements com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep public class [your_pkg].R$*{
    public static final int *;
}

# 热修复混淆
-keep class * extends java.lang.annotation.Annotation
-keep class com.alipay.euler.andfix.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}