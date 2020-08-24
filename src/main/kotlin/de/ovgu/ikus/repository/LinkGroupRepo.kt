package de.ovgu.ikus.repository

import de.ovgu.ikus.model.LinkGroup
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkGroupRepo : CoroutineCrudRepository<LinkGroup, Int>