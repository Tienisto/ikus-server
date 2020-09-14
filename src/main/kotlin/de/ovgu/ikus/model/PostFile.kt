package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

// TODO: make id non-nullable (https://github.com/spring-projects/spring-data-r2dbc/issues/444)
@Table
data class PostFile(@Id var id: Int? = null,
                    var postId: Int? = null,
                    var fileName: String = "",
                    var timestamp: OffsetDateTime = OffsetDateTime.now())