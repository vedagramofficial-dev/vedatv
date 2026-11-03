package com.vedatv.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {

    @Query(
        "SELECT * FROM epg_programs WHERE channelId = :channelId " +
            "AND endTime > :now ORDER BY startTime LIMIT 10"
    )
    fun observePrograms(channelId: String, now: Long): Flow<List<EpgProgramEntity>>

    @Query(
        "SELECT * FROM epg_programs WHERE channelId = :channelId " +
            "AND startTime <= :now AND endTime > :now LIMIT 1"
    )
    fun observeCurrentProgram(channelId: String, now: Long): Flow<EpgProgramEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs")
    suspend fun clear()
}
