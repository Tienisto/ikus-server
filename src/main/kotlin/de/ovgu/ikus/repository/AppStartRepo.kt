package de.ovgu.ikus.repository

import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.StatsType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AppStartRepo : CoroutineCrudRepository<AppStart, Int> {

    suspend fun findFirst90ByTypeOrderByDateDesc(type: StatsType): List<AppStart>
}