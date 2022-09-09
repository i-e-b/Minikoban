# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-optimizations *
-optimizationpasses 5
-overloadaggressively

-keepattributes *Annotation*

-keepclassmembers class ** {
    public void on*(**);
}

# repack obfuscated classes into single package so it would be hard to find their originall package
-repackageclasses ''
-allowaccessmodification

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile