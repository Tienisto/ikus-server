package de.ovgu.ikus.repository

import de.ovgu.ikus.model.PostFile
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostFileRepo : CoroutineCrudRepository<PostFile, Int>