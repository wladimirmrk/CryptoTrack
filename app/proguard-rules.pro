# CryptoTrack release (R8) rules.
#
# Libraries used by the app ship their own consumer rules (Retrofit 3, OkHttp 5,
# kotlinx-serialization 1.11, Room 2.8, Hilt 2.60, Coil 3, Navigation Compose),
# so this file covers only app-specific gaps. Keep it minimal: add a rule only
# when R8 proves it necessary, with a comment explaining why.

# --- kotlinx.serialization -------------------------------------------------
# The runtime's bundled rules keep @Serializable classes and their serializer()
# methods, but in R8 full mode the reflective serializer() lookup on Companion
# objects (used by Json via serializersModule / reified serializer<T>() on
# project DTOs) needs the standard app-side snippet from the official
# kotlinx.serialization documentation.
-keep,includedescriptorclasses class com.cryptotrack.**$$serializer { *; }
-keepclassmembers class com.cryptotrack.** {
    *** Companion;
}
-keepclasseswithmembers class com.cryptotrack.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Navigation Compose (type-safe routes, ADR-004) -------------------------
# Routes are serialized with the fully qualified class name as serialName and
# resolved reflectively at runtime; obfuscated names crash route decoding
# ("Cannot find class with name com.cryptotrack.core.navigation.AboutTopic").
# Keep the names of the route vocabulary in :core:navigation (members may
# still be renamed; serializers are covered by the rules above).
-keepnames class com.cryptotrack.core.navigation.**
