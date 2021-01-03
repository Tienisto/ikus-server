package de.ovgu.ikus.repository

import de.ovgu.ikus.model.AudioFile
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AudioFileRepo : CoroutineCrudRepository<AudioFile, Int> {

    @Query("SELECT MAX(position) FROM audio_file")
    suspend fun findMaxPosition(): Int?

    suspend fun findByOrderByPosition(): List<AudioFile>
    suspend fun findByAudioIdOrderByPosition(audioId: Int): List<AudioFile>
}