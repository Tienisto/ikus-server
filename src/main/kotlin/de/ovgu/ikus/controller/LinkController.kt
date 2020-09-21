package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.LinkGroupWithLinksDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.Link
import de.ovgu.ikus.model.LinkGroup
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/links")
class LinkController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val linkGroupService: LinkGroupService,
        private val linkService: LinkService
) {

    @GetMapping
    suspend fun getAll(): List<LinkGroupWithLinksDto> {
        val groups = linkGroupService.findAllOrdered(IkusLocale.EN)
        val links = linkService.findAllOrdered(IkusLocale.EN)

        return groups.map { group ->
            val groupDto = group.toDto()
            val currLinks = links
                    .filter { link -> link.groupId == group.id }
                    .map { link -> link.toDto(groupDto) }

            LinkGroupWithLinksDto(groupDto, currLinks)
        }
    }

    @PostMapping("/group")
    suspend fun createLinkGroup(authentication: Authentication, @RequestBody request: Request.CreateLinkGroup) {
        val group = linkGroupService.save(LinkGroup(name = request.name.en.trim(), nameDe = request.name.de.trim()))
        logService.log(LogType.CREATE_LINK_GROUP, authentication.toUser(), "${group.name} (${group.nameDe})")
    }

    @PutMapping("/group")
    suspend fun updateLinkGroup(authentication: Authentication, @RequestBody request: Request.UpdateLinkGroup) {
        val group = linkGroupService.findById(request.id) ?: throw ErrorCode(404, "Group not found")
        group.name = request.name.en.trim()
        group.nameDe = request.name.de.trim()
        linkGroupService.save(group)
        logService.log(LogType.UPDATE_LINK_GROUP, authentication.toUser(), "${group.name} (${group.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }

    @DeleteMapping("/group")
    suspend fun deleteLinkGroup(authentication: Authentication, @RequestBody request: Request.Id) {
        val group = linkGroupService.findById(request.id) ?: throw ErrorCode(404, "Group not found")
        linkGroupService.delete(group)
        logService.log(LogType.DELETE_LINK_GROUP, authentication.toUser(), "${group.name} (${group.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }

    @PostMapping
    suspend fun createLink(authentication: Authentication, @RequestBody request: Request.CreateLink) {
        val group = linkGroupService.findById(request.groupId) ?: throw ErrorCode(404, "Group not found")
        val link = Link(groupId = group.id,
                        url = request.url.en.trim(), urlDe = request.url.de.trim(),
                        info = request.info.en.trim(), infoDe = request.info.de.trim())

        linkService.save(link)
        logService.log(LogType.CREATE_LINK, authentication.toUser(), "${link.info} (${link.url})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }

    @PutMapping
    suspend fun updateLink(authentication: Authentication, @RequestBody request: Request.UpdateLink) {
        val group = linkGroupService.findById(request.groupId) ?: throw ErrorCode(404, "Group not found")
        val link = linkService.findById(request.id) ?: throw ErrorCode(404, "Link not found")

        link.groupId = group.id
        link.url = request.url.en.trim()
        link.urlDe = request.url.de.trim()
        link.info = request.info.en.trim()
        link.infoDe = request.info.de.trim()

        linkService.save(link)
        logService.log(LogType.UPDATE_LINK, authentication.toUser(), "${link.info} (${link.url})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }

    @DeleteMapping
    suspend fun deleteLink(authentication: Authentication, @RequestBody request: Request.Id) {
        val link = linkService.findById(request.id) ?: throw ErrorCode(404, "Link not found")
        linkService.delete(link)
        logService.log(LogType.DELETE_LINK, authentication.toUser(), "${link.info} (${link.url})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }
}