package de.ovgu.ikus.service

import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.Link
import de.ovgu.ikus.repository.LinkRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class LinkService (
        private val linkRepo: LinkRepo
) {

    suspend fun findAllOrdered(locale: IkusLocale): List<Link> {
        return when (locale) {
            IkusLocale.EN -> linkRepo.findByOrderByInfo().toList()
            IkusLocale.DE -> linkRepo.findByOrderByInfoDe().toList()
        }
    }

    suspend fun findById(id: Int): Link? {
        return linkRepo.findById(id)
    }

    suspend fun save(link: Link): Link {
        return linkRepo.save(link)
    }

    // dummy only
    suspend fun saveAll(links: List<Link>): List<Link> {
        return linkRepo.saveAll(links).toList()
    }

    suspend fun delete(link: Link) {
        linkRepo.delete(link)
    }
}