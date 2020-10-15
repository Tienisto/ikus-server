package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Feature
import de.ovgu.ikus.model.NativeFeature
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FeatureRepo : CoroutineCrudRepository<Feature, Int> {

    @Query("SELECT MAX(position) FROM feature")
    suspend fun findMaxPosition(): Int?

    suspend fun existsByNativeFeature(feature: NativeFeature): Boolean
    suspend fun findByOrderByPosition(): List<Feature>
}