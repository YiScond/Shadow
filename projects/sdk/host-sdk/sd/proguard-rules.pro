# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontoptimize
-dontpreverify
-dontshrink

#保持需要的类不被混淆
-keep class mobi.oneway.sd.core.runtime.**{*;}
-keep class mobi.oneway.sd.core.manager.ShadowPluginManager{*;}
-keep class mobi.oneway.sd.core.loader.managers.ComponentManager{
    public *;
}

#将没有keep的文件统一放到命名包下面
-flattenpackagehierarchy 'mobi.oneway.sd'

