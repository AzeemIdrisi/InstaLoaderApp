# Chaquopy
-keep class com.chaquo.python.** { *; }

# Instaloader Python package
-keep class com.alphacorp.instaloader.** { *; }

# Encrypted preferences / Tink
-keep class androidx.security.crypto.** { *; }
-dontwarn javax.annotation.**
