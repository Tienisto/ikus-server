package de.ovgu.ikus.utils

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import java.io.ByteArrayOutputStream

suspend fun FilePart.toBytes(): ByteArray {
    val bytesList: List<ByteArray> = this.content()
            .flatMap { dataBuffer -> Flux.just(dataBuffer.asByteBuffer().array()) }
            .collectList()
            .awaitFirst()

    // concat ByteArrays
    val byteStream = ByteArrayOutputStream()
    bytesList.forEach { bytes -> byteStream.write(bytes) }
    return byteStream.toByteArray()
}