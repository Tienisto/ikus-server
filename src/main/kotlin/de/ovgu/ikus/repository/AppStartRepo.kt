package de.ovgu.ikus.repository

import de.ovgu.ikus.model.AppStart
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AppStartRepo : CoroutineCrudRepository<AppStart, Int>