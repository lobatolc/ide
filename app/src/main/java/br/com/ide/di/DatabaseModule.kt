package br.com.ide.di

import android.content.Context
import androidx.room.Room
import br.com.ide.data.local.IdeDatabase
import br.com.ide.data.local.track.MissionTrackPointDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideIdeDatabase(
        @ApplicationContext context: Context
    ): IdeDatabase {

        return Room.databaseBuilder(
            context,
            IdeDatabase::class.java,
            "ide.db"
        ).build()
    }

    @Provides
    fun provideMissionTrackPointDao(
        database: IdeDatabase
    ): MissionTrackPointDao {

        return database.missionTrackPointDao()
    }
}
