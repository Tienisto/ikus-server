package de.ovgu.ikus.service

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.repository.ChannelRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ChannelService (
        private val channelRepo: ChannelRepo
) {

    suspend fun findById(id: Int): Channel? {
        return channelRepo.findById(id)
    }

    suspend fun findByType(type: ChannelType): List<Channel> {
        return channelRepo.findByType(type).toList()
    }

    suspend fun save(channel: Channel): Channel {
        return channelRepo.save(channel)
    }

    suspend fun delete(channel: Channel) {
        channelRepo.delete(channel)
    }
}