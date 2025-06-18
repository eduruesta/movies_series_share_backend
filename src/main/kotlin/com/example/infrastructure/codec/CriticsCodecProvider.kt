package com.example.infrastructure.codec

import com.example.domain.entity.Comment
import com.example.domain.entity.Critics
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

/**
 * Codec personalizado para Critics que maneja la deserialización de comentarios
 */
class CriticsCodecProvider : CodecProvider {
    override fun <T> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        return if (clazz == Critics::class.java) {
            CriticsCodec(registry) as Codec<T>
        } else {
            null
        }
    }
}

class CriticsCodec(private val registry: CodecRegistry) : Codec<Critics> {
    
    private val documentCodec = registry.get(Document::class.java)
    
    override fun getEncoderClass(): Class<Critics> {
        return Critics::class.java
    }

    override fun encode(writer: BsonWriter, value: Critics, encoderContext: EncoderContext) {
        // Convertimos a Document para usar el codec predeterminado
        val doc = Document()
        
        // Copiamos todos los campos
        doc["_id"] = value.id
        doc["title"] = value.title
        doc["rating"] = value.rating
        doc["imageUrl"] = value.imageUrl
        doc["genre"] = value.genre
        doc["platform"] = value.platform
        doc["year"] = value.year
        doc["duration"] = value.duration
        doc["contentRating"] = value.contentRating
        doc["synopsis"] = value.synopsis
        doc["posterUrl"] = value.posterUrl
        doc["ratingCount"] = value.ratingCount
        doc["averageRating"] = value.averageRating
        doc["backdropUrl"] = value.backdropUrl
        doc["groupId"] = value.groupId
        doc["username"] = value.username
        doc["mediaType"] = value.mediaType


        // Convertimos los comentarios al nuevo formato
        val commentsArray = ArrayList<Document>()
        value.comments.forEach { comment ->
            val commentDoc = Document()
            commentDoc["text"] = comment.text
            commentDoc["username"] = comment.username
            commentsArray.add(commentDoc)
        }
        doc["comments"] = commentsArray
        
        // Codificamos usando el codec de Document
        documentCodec.encode(writer, doc, encoderContext)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Critics {
        // Decodificamos usando el codec de Document
        val document = documentCodec.decode(reader, decoderContext)
        
        // Lista para los comentarios convertidos
        val commentsList = mutableListOf<Comment>()
        
        try {
            // Intentamos leer los comentarios como array de documentos (nuevo formato)
            val commentsObj = document["comments"]
            if (commentsObj is List<*>) {
                commentsObj.forEach { commentObj ->
                    if (commentObj is Document) {
                        val text = commentObj["text"]?.toString() ?: ""
                        val username = commentObj["username"]?.toString() ?: "Usuario"
                        commentsList.add(Comment(text = text, username = username))
                    } else if (commentObj is String) {
                        // Si es una string directamente (formato antiguo)
                        commentsList.add(Comment(text = commentObj, username = "Usuario"))
                    }
                }
            } 
            // Si no pudimos convertir, intentamos como List<String> (formato antiguo)
            else if (commentsObj is List<*> && commentsObj.all { it is String }) {
                @Suppress("UNCHECKED_CAST")
                val stringComments = commentsObj as List<String>
                stringComments.forEach { commentText ->
                    commentsList.add(Comment(text = commentText, username = "Usuario"))
                }
            }
        } catch (e: Exception) {
            // Si hay algún error, dejamos la lista vacía
            e.printStackTrace()
        }
        
        // Creamos la entidad Critics con los datos convertidos
        return Critics(
            id = document["_id"] as? Long ?: 0L,
            title = document["title"] as? String ?: "",
            rating = (document["rating"] as? Number)?.toFloat() ?: 0f,
            comments = commentsList,
            imageUrl = document["imageUrl"] as? String ?: "",
            genre = document["genre"] as? String ?: "",
            platform = document["platform"] as? String ?: "",
            year = document["year"] as? String ?: "",
            duration = document["duration"] as? String ?: "",
            contentRating = document["contentRating"] as? String ?: "",
            synopsis = document["synopsis"] as? String ?: "",
            posterUrl = document["posterUrl"] as? String,
            ratingCount = (document["ratingCount"] as? Number)?.toInt() ?: 1,
            averageRating = (document["averageRating"] as? Number)?.toFloat() ?: 0f,
            backdropUrl = document["backdropUrl"] as? String,
            groupId = document["groupId"] as? String,
            username = document["username"] as? String ?: "",
            mediaType = document["mediaType"] as? String ?: ""
        )
    }
}
