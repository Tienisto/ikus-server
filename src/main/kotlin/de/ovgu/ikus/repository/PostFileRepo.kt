package de.ovgu.ikus.repository

import de.ovgu.ikus.model.PostFile
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostFileRepo : CoroutineCrudRepository<PostFile, Int> {

    fun findByIdIn(ids: List<Int>): Flow<PostFile>
}