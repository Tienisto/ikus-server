package de.ovgu.ikus.service

import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.utils.toJSON
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.time.Duration

enum class CacheKey {
    NEWS,
    CALENDAR,
    MENSA,
    LINKS,
    HANDBOOK_BOOKMARKS,
    FAQ,
    CONTACTS,
    APP_CONFIG
}
private val lifeTime = Duration.ofMinutes(5).toMillis()
data class CachedData(var json: String?, var needUpdate: Boolean, var expires: Long)

@Service
class CacheService {

    // all cached json are stored in this map
    private val map: Map<Pair<CacheKey, IkusLocale>, CachedData> = CacheKey
            .values()
            .map { key -> IkusLocale.values().map { locale -> Pair(key, locale) to CachedData(null, true, 0) } }
            .flatten()
            .toMap()

    suspend fun getCacheOrUpdate(key: CacheKey, locale: IkusLocale, updateFunc: suspend () -> Any): String {
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
     * the next call of [getCacheOrUpdate] will call the update function
     */
    fun triggerUpdateFlag(key: CacheKey) {
        IkusLocale.values().forEach { locale ->
            map[Pair(key, locale)]!!.needUpdate = true
        }
    }

}