package de.ovgu.ikus.service

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Link
import de.ovgu.ikus.repository.LinkRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class LinkService (
        private val linkRepo: LinkRepo
) {

    suspend fun findAllOrdered(): List<Link> {
        return linkRepo.findByOrderByPosition().toList()
    }

    suspend fun findByChannelId(channelId: Int): List<Link> {
        return linkRepo.findByChannelIdOrderByPosition(channelId).toList()
    }

    suspend fun search(title: String): List<Link> {
        return linkRepo.findByInfoIgnoreCase("%$title%").toList()
    }

    suspend fun findById(id: Int): Link? {
        return linkRepo.findById(id)
    }

    suspend fun existsById(id: Int): Boolean {
        return linkRepo.existsById(id)
    }

    suspend fun save(link: Link): Link {
        return linkRepo.save(link)
    }

    // dummy or when changing order
    suspend fun saveAll(links: List<Link>): List<Link> {
        return linkRepo.saveAll(links).toList()
    }

    suspend fun delete(link: Link) {
        linkRepo.delete(link)
    }

    suspend fun findMaxPositionByChannel(channel: Channel): Int {
        return linkRepo.findMaxPositionByChannelId(channel.id) ?: -1
    }
}