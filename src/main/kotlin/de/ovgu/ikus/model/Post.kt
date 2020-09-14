package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

enum class PostType {
    NEWS, FAQ
}

// TODO: make id non-nullable (https://github.com/spring-projects/spring-data-r2dbc/issues/444)
@Table
data class Post(@Id var id: Int? = null,
                var type: PostType = PostType.NEWS,
                var channelId: Int = 0,
                var date: LocalDate = LocalDate.now(),
                var title: String = "",
                var titleDe: String = "",
                var content: String = "",
                var contentDe: String = "")