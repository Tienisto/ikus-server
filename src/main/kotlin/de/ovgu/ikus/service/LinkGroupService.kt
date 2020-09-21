package de.ovgu.ikus.service

import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LinkGroup
import de.ovgu.ikus.repository.LinkGroupRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class LinkGroupService (
        private val linkGroupRepo: LinkGroupRepo
) {

    suspend fun findAllOrdered(locale: IkusLocale): List<LinkGroup> {
        return when (locale) {
            IkusLocale.EN -> linkGroupRepo.findByOrderByName().toList()
            IkusLocale.DE -> linkGroupRepo.findByOrderByNameDe().toList()
        }
    }

    suspend fun findById(id: Int): LinkGroup? {
        return linkGroupRepo.findById(id)
    }

    suspend fun save(group: LinkGroup): LinkGroup {
        return linkGroupRepo.save(group)
    }

    // dummy only
    suspend fun saveAll(groups: List<LinkGroup>): List<LinkGroup> {
        return linkGroupRepo.saveAll(groups).toList()
    }

    suspend fun delete(group: LinkGroup) {
        linkGroupRepo.delete(group)
    }

    suspend fun deleteAll() {
        linkGroupRepo.deleteAll()
    }
}