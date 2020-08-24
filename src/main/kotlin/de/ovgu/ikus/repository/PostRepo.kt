package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Post
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int>