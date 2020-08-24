package de.ovgu.ikus.repository

import de.ovgu.ikus.model.HandbookBookmark
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface HandbookBookmarkRepo : CoroutineCrudRepository<HandbookBookmark, Int>