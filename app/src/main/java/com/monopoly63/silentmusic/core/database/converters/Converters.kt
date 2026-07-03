package com.monopoly63.silentmusic.core.database.converters

import androidx.room.TypeConverter
import com.monopoly63.silentmusic.core.database.entities.FolderRuleType

class Converters {
    @TypeConverter fun fromRuleType(type: FolderRuleType): String = type.name
    @TypeConverter fun toRuleType(value: String): FolderRuleType = FolderRuleType.valueOf(value)
}
