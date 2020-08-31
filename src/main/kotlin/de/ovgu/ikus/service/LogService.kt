package de.ovgu.ikus.service

import de.ovgu.ikus.dto.LogDto
import de.ovgu.ikus.dto.UserDto
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.LogRepo
import de.ovgu.ikus.repository.UserRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class LogService (
        private val logRepo: LogRepo,
        private val userRepo: UserRepo
) {

    suspend fun findAll(limit: Int): List<LogDto> {
        val raw = logRepo.findAllWithLimit(limit).toList()
        val users = userRepo.findAll().toList()
        return raw.map { r ->
            val user = users
                    .firstOrNull { user -> r.id == user.id }
                    ?.let { user -> UserDto(user.id, user.name) }

            LogDto(user, r.timestamp, r.type, r.info)
        }
    }

    suspend fun log(type: LogType, user: User, info: String) {
        val instance = Log(type = type, userId = user.id, info = info)
        logRepo.save(instance)
    }
}