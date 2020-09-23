package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

enum class StatsType {
    DAILY, WEEKLY, MONTHLY
}

@Table
data class AppStart(@Id var id: Int = 0,
                    var type: StatsType = StatsType.DAILY,
                    var date: LocalDate = LocalDate.now(),
                    var android: Int = 0,
                    var ios: Int = 0)