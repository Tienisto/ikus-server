package de.ovgu.ikus.service

import de.ovgu.ikus.dto.LogDto
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.LogRepo
import de.ovgu.ikus.repository.UserRepo
import de.ovgu.ikus.utils.toDto
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class LogService (
        private val logRepo: LogRepo,
        private val userRepo: UserRepo
) {

    suspend fun findAll(limit: Int): List<LogDto> {
        val logs = logRepo.findAllWithLimit(limit)
        val users = userRepo.findAll().toList()
        return logs.map { log ->
            val user = users.firstOrNull { user -> log.userId == user.id }?.toDto()
            log.toDto(user)
        }
    }

    suspend fun log(type: LogType, user: User, info: String) {
        val instance = Log(type = type, userId = user.id, info = info)
        logRepo.save(instance)
    }

    suspend fun deleteAll() {
        logRepo.deleteAll()
    }
}