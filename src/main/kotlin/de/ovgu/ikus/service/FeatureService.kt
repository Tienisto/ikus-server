package de.ovgu.ikus.service

import de.ovgu.ikus.model.Feature
import de.ovgu.ikus.model.NativeFeature
import de.ovgu.ikus.repository.FeatureRepo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class FeatureService (
        private val featureRepo: FeatureRepo
) {

    /**
     * ensure that all native features are listed in the table
     */
    suspend fun repairNativeFeatures() {
        NativeFeature.values().forEach { feature ->
            if (!featureRepo.existsByNativeFeature(feature)) {
                // add missing native feature
                val maxPosition = findMaxPosition()
                featureRepo.save(Feature(
                        position = maxPosition + 1, favorite = feature.favorite,
                        nativeFeature = feature
                ))
            }
        }
    }

    suspend fun findById(id: Int): Feature? {
        return featureRepo.findById(id)
    }

    suspend fun findAllOrderByPosition(): List<Feature> {
        return featureRepo.findByOrderByPosition().toList()
    }

    suspend fun save(feature: Feature): Feature {
        return featureRepo.save(feature)
    }

    suspend fun saveAll(files: List<Feature>) {
        featureRepo.saveAll(files).collect()
    }

    suspend fun delete(feature: Feature) {
        featureRepo.delete(feature)
    }

    suspend fun deleteAll() {
        featureRepo.deleteAll()
    }

    suspend fun findMaxPosition(): Int {
        return featureRepo.findMaxPosition() ?: -1
    }
}