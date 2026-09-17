package br.com.ide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.ide.data.local.track.MissionTrackPointDao
import br.com.ide.data.local.track.MissionTrackPointEntity

@Database(
    entities = [MissionTrackPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IdeDatabase : RoomDatabase() {

    abstract fun missionTrackPointDao(): MissionTrackPointDao
}
