package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Link
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkRepo : CoroutineCrudRepository<Link, Int>