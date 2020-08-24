package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChannelRepo : CoroutineCrudRepository<Channel, Int>