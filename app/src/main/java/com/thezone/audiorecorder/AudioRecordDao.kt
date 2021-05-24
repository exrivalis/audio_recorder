package com.thezone.audiorecorder

import androidx.room.*

@Dao
interface AudioRecordDao {
    @Query("SELECT * FROM audioRecords")
    fun getAll(): List<AudioRecord>

    @Insert
    fun insert(vararg audioRecord: AudioRecord)

    @Delete
    fun delete(audioRecord: AudioRecord)

    @Delete
    fun delete(audioRecords: Array<AudioRecord>)

    @Update
    fun update(audioRecord: AudioRecord)

}