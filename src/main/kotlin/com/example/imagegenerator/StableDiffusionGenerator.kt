package com.example.imagegenerator

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class StableDiffusionGenerator(private val apiKey: String) {
    private val logger = LoggerFactory.getLogger(StableDiffusionGenerator::class.java)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // Longer timeout for image generation
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val mapper = jacksonObjectMapper()
    private val baseUrl = "https://api.stability.ai/v1"
    
    fun validateApiKey(): Boolean {
        return try {
            logger.info("Validating Stability AI API key...")
            
            val request = Request.Builder()
                .url("$baseUrl/user/balance")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                logger.info("✅ API key is valid")
                if (responseBody != null) {
                    try {
                        val balance = mapper.readValue<BalanceResponse>(responseBody)
                        logger.info("💰 Account balance: ${balance.credits} credits")
                        if (balance.credits < 1) {
                            logger.warn("⚠️  Low credit balance. Consider purchasing more credits.")
                        }
                    } catch (e: Exception) {
                        logger.info("📋 Account info retrieved (balance format not recognized)")
                    }
                }
                true
            } else {
                logger.error("❌ API key validation failed with code: ${response.code}")
                val responseBody = response.body?.string()
                logger.error("Response: $responseBody")
                
                // Provide specific guidance based on error
                when (response.code) {
                    401 -> {
                        logger.error("🔑 AUTHENTICATION ERROR:")
                        logger.error("The API key is invalid or expired.")
                        logger.error("Solutions:")
                        logger.error("1. Get a fresh API key from: https://platform.stability.ai/account/keys")
                        logger.error("2. Make sure you're copying the full key")
                        logger.error("3. Check if your account is active")
                        logger.error("4. Try logging out and back in to refresh your session")
                    }
                    403 -> {
                        logger.error("🚫 ACCESS DENIED:")
                        logger.error("Your account may be suspended or have restrictions.")
                        logger.error("Check your account status at: https://platform.stability.ai/account")
                    }
                    else -> {
                        logger.error("❓ UNKNOWN ERROR: HTTP ${response.code}")
                        logger.error("Please check your internet connection and try again.")
                    }
                }
                false
            }
            
        } catch (e: Exception) {
            logger.error("❌ Error validating API key: ${e.message}")
            logger.error("Please check your internet connection and API key format.")
            false
        }
    }
    
    fun generateImage(prompt: String, size: String, outputPath: String, model: String): Boolean {
        return try {
            logger.info("Sending request to Stability AI API...")
            
            // Parse size to width and height
            val (width, height) = parseSize(size)
            
            val requestBody = StableDiffusionRequest(
                textPrompts = listOf(
                    TextPrompt(text = prompt, weight = 1.0f)
                ),
                cfgScale = 7.0f,
                height = height,
                width = width,
                samples = 1,
                steps = 30,
                stylePreset = "photographic"
            )
            
            val jsonBody = mapper.writeValueAsString(requestBody)
            logger.debug("Request body: $jsonBody")
            
            val request = Request.Builder()
                .url("$baseUrl/generation/$model/text-to-image")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(RequestBody.create("application/json".toMediaType(), jsonBody))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                logger.error("API request failed with code: ${response.code}")
                logger.error("Response headers: ${response.headers}")
                val responseBody = response.body?.string()
                logger.error("Response body: $responseBody")
                
                // Provide specific guidance for common errors
                when (response.code) {
                    400 -> {
                        if (responseBody?.contains("insufficient_credits") == true) {
                            logger.error("INSUFFICIENT CREDITS:")
                            logger.error("Your Stability AI account doesn't have enough credits.")
                            logger.error("Solutions:")
                            logger.error("1. Check your balance at: https://platform.stability.ai/account/credits")
                            logger.error("2. Purchase more credits if needed")
                            logger.error("3. Try using a different API key")
                        } else if (responseBody?.contains("invalid_api_key") == true) {
                            logger.error("INVALID API KEY:")
                            logger.error("The provided API key is not valid.")
                            logger.error("Please check your API key at: https://platform.stability.ai/account/keys")
                        }
                    }
                    401 -> {
                        logger.error("AUTHENTICATION ERROR:")
                        logger.error("Invalid API key or authentication failed.")
                        logger.error("Please verify your API key is correct.")
                    }
                    429 -> {
                        logger.error("RATE LIMIT EXCEEDED:")
                        logger.error("Too many requests. Please wait and try again.")
                    }
                    else -> {
                        logger.error("UNKNOWN ERROR: HTTP ${response.code}")
                    }
                }
                return false
            }
            
            val responseBody = response.body?.string()
            logger.debug("Response body: $responseBody")
            
            val imageResponse = mapper.readValue<StableDiffusionResponse>(responseBody!!)
            
            if (imageResponse.artifacts.isNotEmpty()) {
                val imageData = imageResponse.artifacts[0].base64
                logger.info("Image data received, saving to file...")
                
                saveBase64Image(imageData, outputPath)
            } else {
                logger.error("No image data received from API")
                false
            }
            
        } catch (e: Exception) {
            logger.error("Error generating image: ${e.message}", e)
            false
        }
    }
    
    private fun parseSize(size: String): Pair<Int, Int> {
        return when (size) {
            "256x256" -> Pair(256, 256)
            "512x512" -> Pair(512, 512)
            "1024x1024" -> Pair(1024, 1024)
            "1024x768" -> Pair(1024, 768)
            "768x1024" -> Pair(768, 1024)
            else -> {
                logger.warn("Unknown size format: $size, using default 1024x1024")
                Pair(1024, 1024)
            }
        }
    }
    
    private fun saveBase64Image(base64Data: String, outputPath: String): Boolean {
        return try {
            logger.info("Saving image to: $outputPath")
            
            val imageBytes = java.util.Base64.getDecoder().decode(base64Data)
            File(outputPath).writeBytes(imageBytes)
            
            logger.info("✅ Image saved successfully!")
            true
            
        } catch (e: Exception) {
            logger.error("Error saving image: ${e.message}", e)
            false
        }
    }
    
    fun getAvailableModels(): List<String> {
        return listOf(
            "stable-diffusion-v1-6",
            "stable-diffusion-xl-1024-v1-0",
            "stable-diffusion-xl-beta-v2-2-2",
            "deepfloyd-if-v1-0",
            "stable-diffusion-2-1",
            "stable-diffusion-2-1-base"
        )
    }
}

// Data classes for Stability AI API
data class StableDiffusionRequest(
    val textPrompts: List<TextPrompt>,
    val cfgScale: Float,
    val height: Int,
    val width: Int,
    val samples: Int,
    val steps: Int,
    val stylePreset: String
)

data class TextPrompt(
    val text: String,
    val weight: Float
)

data class StableDiffusionResponse(
    val artifacts: List<Artifact>
)

data class Artifact(
    val base64: String,
    val finishReason: String,
    val seed: Long
)

data class BalanceResponse(
    val credits: Float
) 