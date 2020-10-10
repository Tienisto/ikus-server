package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Feature
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FeatureRepo : CoroutineCrudRepository<Feature, Int>