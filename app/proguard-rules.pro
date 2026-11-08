# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Hasbi data classes (Room entities)
-keep class com.hasbi.database.entity.** { *; }
-keep class com.hasbi.domain.model.** { *; }

# Type converters
-keep class com.hasbi.database.converter.** { *; }

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
