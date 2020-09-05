package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class PostFile(@Id var id: Int = 0,
                    var postId: Int = 0,
                    var fileName: String = "",
                    var mime: String = "",
                    var size: Long = 0)