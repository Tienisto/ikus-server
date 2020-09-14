package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.properties.StorageProperties
import de.ovgu.ikus.utils.getMime
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class FileService (
        private val propsStorage: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(FileService::class.java)

    fun hasReadAccess(): Boolean {
        return checkAccess { path -> Files.isReadable(path) }
    }

    fun hasWriteAccess(): Boolean {
        return checkAccess { path -> Files.isWritable(path) }
    }

    private fun checkAccess(func: (path: Path) -> Boolean): Boolean {
        try {
            val path = Paths.get(propsStorage.path)
            Files.createDirectories(path)
            return func(path)
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun storeFilePart(file: FilePart, path: String, absolute: Boolean = false) {
        try {
            // Copy file to the target location
            val targetLocation = when (absolute) {
                true -> Paths.get(normalize(path))
                else -> Paths.get(propsStorage.path + "/" + normalize(path))
            }

            Files.createDirectories(targetLocation.parent)
            file.transferTo(targetLocation).awaitFirstOrNull()
        } catch (ex: IOException) {
            logger.error("Storing file failed",ex)
        }
    }

    fun loadFile(path: String, webExchange: ServerWebExchange): FileSystemResource {
        val resource = FileSystemResource(propsStorage.path + "/" + normalize(path))
        if (!resource.exists())
            throw ErrorCode(404, "File not found")

        webExchange.response.headers["Content-Type"] = getMime(path)
        return resource
    }

    fun normalize(path: String): String {
        return path.replace("\\", "/")
    }
}