package de.ovgu.ikus.service

import de.ovgu.ikus.properties.StorageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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

    fun storeFile(inputStream: InputStream, path: String, absolute: Boolean = false) {
        try {
            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = when (absolute) {
                true -> Paths.get(normalize(path))
                else -> Paths.get(propsStorage.path + "/" + normalize(path))
            }

            Files.createDirectories(targetLocation.parent)
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        } catch (ex: IOException) {
            logger.error("Storing file failed",ex)
        }
    }

    fun normalize(path: String): String {
        return path.replace("\\", "/")
    }
}