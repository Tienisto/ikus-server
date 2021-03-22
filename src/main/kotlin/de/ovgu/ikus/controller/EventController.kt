package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.Event
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toDto
import de.ovgu.ikus.utils.toPoint
import de.ovgu.ikus.utils.trimOrNull
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val eventService: EventService,
        private val channelService: ChannelService
) {

    @GetMapping("/all")
    suspend fun getAll(): List<EventDto> {
        val channelsDtoMap = channelService
                .findByType(ChannelType.CALENDAR)
                .map { channel -> channel.id to channel.toDto() }
                .toMap()

        return eventService.findAllOrdered().map { event ->
            val channel = channelsDtoMap[event.channelId] ?: ChannelDto(0, MultiLocaleString("Error", "Error"))
            event.toDto(channel)
        }
    }

    @GetMapping
    suspend fun getByChannel(@RequestParam channelId: Int): List<EventDto> {
        val channel = channelService.findById(channelId) ?: throw ErrorCode(404, "Channel not found")
        val channelDto = channel.toDto()

        return eventService.findByChannelOrdered(channel).map { event -> event.toDto(channelDto) }
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: Int): EventDto {
        val event = eventService.findById(id) ?: throw ErrorCode(404, "Event not found")
        val channel = channelService.findById(event.channelId) ?: throw ErrorCode(500, "Channel not found")
        return event.toDto(channel.toDto())
    }

    @PostMapping
    suspend fun createEvent(authentication: Authentication, @RequestBody request: Request.CreateEvent) {
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")
        if (channel.type != ChannelType.CALENDAR)
            throw ErrorCode(409, "Wrong Channel Type")

        val event = Event (
                channelId = channel.id,
                place = request.place?.trimOrNull(), coords = request.coords?.toPoint(),
                startTime = request.startTime, endTime = request.endTime,
                name = request.name.en.trim(), nameDe = request.name.de.trim(),
                info = request.info?.en?.trimOrNull(), infoDe = request.info?.de?.trimOrNull()
        )

        eventService.save(event)
        logService.log(LogType.CREATE_EVENT, authentication.toUser(), "${event.name} (${event.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
    }

    @PutMapping
    suspend fun updateEvent(authentication: Authentication, @RequestBody request: Request.UpdateEvent) {
        val event = eventService.findById(request.id) ?: throw ErrorCode(404, "Event not found")
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")
        if (channel.type != ChannelType.CALENDAR)
            throw ErrorCode(409, "Wrong Channel Type")

        // apply
        event.channelId = channel.id
        event.place = request.place?.trimOrNull()
        event.coords = request.coords?.toPoint()
        event.startTime = request.startTime
        event.endTime = request.endTime
        event.name = request.name.en.trim()
        event.nameDe = request.name.de.trim()
        event.info = request.info?.en?.trimOrNull()
        event.infoDe = request.info?.de?.trimOrNull()

        eventService.save(event)
        logService.log(LogType.UPDATE_EVENT, authentication.toUser(), "${event.name} (${event.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
    }

    @DeleteMapping
    suspend fun deleteEvent(authentication: Authentication, @RequestBody request: Request.Id) {
        val event = eventService.findById(request.id) ?: throw ErrorCode(404, "Event not found")

        eventService.delete(event)
        logService.log(LogType.DELETE_EVENT, authentication.toUser(), "${event.name} (${event.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
    }

    @PostMapping("/registrations")
    suspend fun updateRegistrationRequirements(authentication: Authentication, @RequestBody payload: Request.UpdateEventRegistrationInfo) {
        val event = eventService.findById(payload.id) ?: throw ErrorCode(404, "Event not found")

        event.registrationFields = payload.fields.map { field -> field.toString() }
        event.registrationSlots = payload.slots
        event.registrationSlotsWaiting = payload.slotsWaiting
        event.registrationOpen = payload.open
        eventService.save(event)

        val type = if(payload.open) LogType.UPDATE_EVENT_OPEN_REGISTRATION else LogType.UPDATE_EVENT_CLOSE_REGISTRATION
        logService.log(type, authentication.toUser(), "${event.name} (${event.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
    }

    @PostMapping("/registrations/kick")
    suspend fun kickRegistration(@RequestBody payload: Request.KickEventRegistration) {
        val event = eventService.findById(payload.eventId) ?: throw ErrorCode(404, "Event not found")

        val changed = eventService.removeRegisteredUser(event, payload.token)
        if (changed) {
            cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
        }
    }
}