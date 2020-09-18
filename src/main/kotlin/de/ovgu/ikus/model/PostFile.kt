package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table
data class PostFile(@Id var id: Int = 0,
                    var postId: Int? = null,
                    var fileName: String = "",
                    var timestamp: OffsetDateTime = OffsetDateTime.now())