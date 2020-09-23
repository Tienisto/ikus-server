package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

enum class LogType {
    CREATE_USER, UPDATE_USER_NAME, UPDATE_USER_PASSWORD, DELETE_USER,
    CREATE_CHANNEL, UPDATE_CHANNEL, DELETE_CHANNEL,
    CREATE_POST, UPDATE_POST, DELETE_POST,
    CREATE_EVENT, UPDATE_EVENT, DELETE_EVENT,
    CREATE_LINK_GROUP, UPDATE_LINK_GROUP, DELETE_LINK_GROUP,
    CREATE_LINK, UPDATE_LINK, DELETE_LINK,
    UPDATE_HANDBOOK, UPDATE_HANDBOOK_BOOKMARKS,
    CREATE_CONTACT, UPDATE_CONTACT, DELETE_CONTACT
}

@Table
data class Log(@Id var id: Int = 0,
               var userId: Int? = null,
               var timestamp: OffsetDateTime = OffsetDateTime.now(),
               var type: LogType = LogType.CREATE_USER,
               var info: String = "")