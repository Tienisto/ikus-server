package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Log
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LogRepo : CoroutineCrudRepository<Log, Int>