package com.mustafakoceerr.justrelax.feature.ai.data.repository

import com.mustafakoceerr.justrelax.feature.ai.BuildConfig
import com.mustafakoceerr.justrelax.feature.ai.data.model.*
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class OpenAiServiceImpl(
    private val client: HttpClient,
    private val soundRepository: SoundRepository,
    private val json: Json
) : AiService {

    private companion object {
        const val ENDPOINT = "https://api.openai.com/v1/chat/completions"
    }

    private val apiKey = BuildConfig.OPENAI_API_KEY

    override suspend fun generateMix(userPrompt: String): Result<AiMixResponse> {
        return runCatching {
            val inventory = buildSoundInventory()
            val systemInstruction = buildSystemInstruction(inventory)
            val requestBody = buildRequestBody(systemInstruction, userPrompt)

            val response = client.post(ENDPOINT) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.body<String>()
                throw IllegalStateException(
                    "OpenAI API error: ${response.status} - $errorBody"
                )
            }

            val openAiResponse = response.body<OpenAiResponse>()
            val content = openAiResponse.choices.first().message.content

            json.decodeFromString<AiMixResponse>(content)
        }
    }

    private suspend fun buildSoundInventory(): String {
        val downloadedSounds = soundRepository.getDownloadedSounds().first()

        require(downloadedSounds.isNotEmpty()) {
            "No sounds available"
        }

        return downloadedSounds.joinToString(", ") {
            "${it.id}:${it.category.name}"
        }
    }

    private fun buildSystemInstruction(inventory: String): String =
        """
        You are an expert Soundscape Architect & DJ.
        Your goal is to create immersive, realistic, and mood-appropriate audio mixes using ONLY the provided sound inventory.

        ### 1. INVENTORY (ID:Category)
        [$inventory]

        ### 2. MIXING RULES (Critical)
        - **Layering:** Do not set all volumes to 1.0. Create depth.
          - Base Layer (Rain, Wind, White Noise): 0.6 – 0.9
          - Accent Layer (Birds, Thunder, Chimes): 0.3 – 0.6
        - **Variety:** Use 3–5 sounds.
        - **Relevance:** Avoid loud or sharp sounds for sleep-related requests.

        ### 3. EXAMPLE
        User: "I need to focus on my coding."
        Mix: "Deep Flow"
        Sounds: heavy_rain (0.8), crackling_fire (0.4), coffee_shop (0.3)

        Now generate the mix for the real user request.
        """.trimIndent()

    private fun buildRequestBody(
        systemInstruction: String,
        userPrompt: String
    ): String {
        val schema = buildMixSchema()

        val request = OpenAiRequest(
            messages = listOf(
                OpenAiMessage("system", systemInstruction),
                OpenAiMessage("user", userPrompt)
            ),
            responseFormat = OpenAiResponseFormat(
                jsonSchema = JsonSchemaDefinition(
                    name = "mix_schema",
                    schema = schema
                )
            )
        )

        return json.encodeToString(request)
    }

    private fun buildMixSchema() = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {

            putJsonObject("mix_name") {
                put("type", "string")
                put("description", "Creative, short title")
            }

            putJsonObject("description") {
                put("type", "string")
                put("description", "One sentence describing the vibe")
            }

            putJsonObject("sounds") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "object")
                    putJsonObject("properties") {

                        putJsonObject("id") {
                            put("type", "string")
                            put("description", "Exact ID from inventory")
                        }

                        putJsonObject("volume") {
                            put("type", "number")
                            put("description", "Range: 0.1 – 1.0")
                        }
                    }

                    putJsonArray("required") {
                        add("id")
                        add("volume")
                    }

                    put("additionalProperties", false)
                }
            }
        }

        putJsonArray("required") {
            add("mix_name")
            add("description")
            add("sounds")
        }

        put("additionalProperties", false)
    }
}