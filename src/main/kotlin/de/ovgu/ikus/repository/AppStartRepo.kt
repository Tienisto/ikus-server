package de.ovgu.ikus.repository

import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.StatsType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AppStartRepo : CoroutineCrudRepository<AppStart, Int> {

    fun findByTypeOrderByDate(type: StatsType): Flow<AppStart>
}