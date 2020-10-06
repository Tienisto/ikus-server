package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.properties.StorageProperties
import de.ovgu.ikus.utils.getMime
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

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

    fun storeFileInputStream(input: InputStream, path: String, absolute: Boolean = false) {
        try {
            // Copy file to the target location
            val targetLocation = when (absolute) {
                true -> Paths.get(normalize(path))
                else -> Paths.get(propsStorage.path + "/" + normalize(path))
            }

            Files.createDirectories(targetLocation.parent)
            Files.copy(input, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        } catch (ex: IOException) {
            logger.error("Storing file failed",ex)
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

    fun loadFile(path: String, download: Boolean, webExchange: ServerWebExchange): FileSystemResource? {
        val resource = FileSystemResource(propsStorage.path + "/" + normalize(path))
        if (!resource.exists())
            throw ErrorCode(404, "File not found")

        val clientLastModified = webExchange.request.headers.ifModifiedSince
        if (clientLastModified != -1L) {
            // client has given If-Modified-Since, so lets check
            val resourceLastModified = (resource.lastModified() / 1000) * 1000 // ignore millis
            if (resourceLastModified <= clientLastModified) {
                webExchange.response.statusCode = HttpStatus.NOT_MODIFIED
                webExchange.response.headers.setDate("Last-Modified", resourceLastModified)
                return null
            }
        }

        webExchange.response.headers["Content-Type"] = getMime(path)
        webExchange.response.headers["Content-Disposition"] = if (download) "attachment" else "inline"
        return resource
    }

    fun deleteFile(path: String) {
        val pathObj = Paths.get(propsStorage.path + "/" + normalize(path))
        Files.delete(pathObj)
    }

    fun normalize(path: String): String {
        return path.replace("\\", "/")
    }
}