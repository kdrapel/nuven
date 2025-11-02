package com.kdr.nuven

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * New page format based on pageformat.json specification
 */
@Serializable
data class PageFormat(
    val pageId: String,
    val title: String,
    val version: String,
    val publishedAt: String,
    val globalStyles: GlobalStyles,
    val sections: List<Section>,
    val links: List<Link> = emptyList(),
    val metadata: Metadata
)

@Serializable
data class GlobalStyles(
    val defaultForeground: String,
    val defaultBackground: String,
    val defaultFont: String,
    val maxPageWidth: Int,
    val maxPageHeight: Int
)

@Serializable
data class Section(
    val id: String,
    val type: String, // header, status, text-block
    val position: Position,
    val styles: SectionStyles,
    val content: SectionContent
)

@Serializable
data class Position(
    val row: Int,
    val column: Int
)

@Serializable
data class SectionStyles(
    val foreground: String,
    val background: String? = null,
    val maxWidth: Int? = null,
    val blockHeight: Int? = null,
    val textAlign: String? = null
)

@Serializable
data class Link(
    val text: String,
    val pageId: String
)

@Serializable
data class Metadata(
    val language: String,
    val category: String,
    val source: String? = null,
    val url: String? = null,
    val longTitle: String? = null,
    val mediumTitle: String? = null,
    val shortTitle: String? = null
)

// Helper for content which can be String or List<String>
@Serializable(with = SectionContentSerializer::class)
sealed class SectionContent {
    data class Text(val value: String) : SectionContent()
    data class Lines(val value: List<String>) : SectionContent()
}

object SectionContentSerializer : KSerializer<SectionContent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SectionContent")

    override fun deserialize(decoder: Decoder): SectionContent {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        
        return when {
            element is JsonPrimitive && element.isString -> 
                SectionContent.Text(element.content)
            element is JsonArray -> 
                SectionContent.Lines(element.map { it.jsonPrimitive.content })
            else -> 
                SectionContent.Text(element.toString())
        }
    }

    override fun serialize(encoder: Encoder, value: SectionContent) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            is SectionContent.Text -> jsonEncoder.encodeString(value.value)
            is SectionContent.Lines -> jsonEncoder.encodeJsonElement(
                JsonArray(value.value.map { JsonPrimitive(it) })
            )
        }
    }
}
