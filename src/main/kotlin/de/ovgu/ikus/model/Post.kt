package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

enum class PostType {
    NEWS, FAQ
}

@Table
data class Post(@Id var id: Int = 0,
                var type: PostType = PostType.NEWS,
                var channelId: Int = 0,
                var date: LocalDate = LocalDate.now(),
                var title: String = "",
                var titleDe: String = "",
                var content: String = "",
                var contentDe: String = "",
                var position: Int = 0,
                var pinned: Boolean = false)