package de.ovgu.ikus.service

import de.ovgu.ikus.properties.StorageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
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
        val path = getAndValidateMainPath() ?: return false
        Files.createDirectories(path)
        return Files.isReadable(File(propsStorage.path).toPath())
    }

    fun hasWriteAccess(): Boolean {
        val path = getAndValidateMainPath() ?: return false
        Files.createDirectories(path)
        return Files.isWritable(File(propsStorage.path).toPath())
    }

    private fun getAndValidateMainPath(): Path? {
        try {
            return Paths.get(propsStorage.path)
        } catch (e: Exception) {
            return null
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