package de.ovgu.ikus.service

import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.utils.toJSON
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.time.Duration

enum class CacheKey {
    NEWS,
    CALENDAR
}
private val lifeTime = Duration.ofMinutes(1).toMillis()
data class CachedData(var json: String?, var needUpdate: Boolean, var expires: Long)

@Service
class CacheService {

    private val map: Map<Pair<CacheKey, IkusLocale>, CachedData> = CacheKey
            .values()
            .map { key -> IkusLocale.values().map { locale -> Pair(key, locale) to CachedData(null, true, System.currentTimeMillis() + lifeTime) } }
            .flatten()
            .toMap()

    suspend fun getOrUpdate(key: CacheKey, locale: IkusLocale, updateFunc: suspend () -> Any): String {
        val cached = map[Pair(key, locale)]!!

        return when {
            cached.needUpdate || System.currentTimeMillis() > cached.expires -> {
                cached.needUpdate = false
                cached.expires = System.currentTimeMillis() + lifeTime
                val newData = updateFunc().toJSON()
                cached.json = newData
                newData
            }
            else -> {
                var json: String? = cached.json
                while (json == null) {
                    // wait until first data arrives
                    delay(100)
                    json = cached.json
                }
                json
            }
        }
    }

    /**
     * the next call of [getOrUpdate] will call the update function
     */
    fun triggerUpdateFlag(key: CacheKey, locale: IkusLocale) {
        map[Pair(key, locale)]!!.needUpdate = true
    }

}