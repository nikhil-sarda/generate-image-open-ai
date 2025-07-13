package com.example.imagegenerator

/**
 * AIML API Generator for image generation
 * 
 * CONFIGURED FOR AIML API:
 * This generator is configured with the actual AIML API endpoints and request format.
 * 
 * API Endpoints:
 * - Base URL: https://api.aimlapi.com
 * - Image Generation: /v1/images/generations
 * - Validation: /v1/validate (TODO: verify this endpoint)
 * 
 * Request Format: Based on actual AIML API specification
 * Response Format: URL-based image response
 * 
 * Available Models:
 * - openai/gpt-image-1 (DALL-E 3 equivalent)
 * - openai/gpt-image-2 (DALL-E 2 equivalent)
 * - stability-ai/stable-diffusion-xl-1024-v1-0
 * - stability-ai/stable-diffusion-v1-6
 * - midjourney/midjourney-v6
 */

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class AimlApiGenerator(private val apiKey: String) {
    private val logger = LoggerFactory.getLogger(AimlApiGenerator::class.java)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // Longer timeout for image generation
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val mapper = jacksonObjectMapper()
    private val baseUrl = "https://api.aimlapi.com" // AIML API base URL
    
    // AIML API endpoints
    private val validateEndpoint = "/v1/validate" // TODO: Find actual validation endpoint
    private val imageGenerationEndpoint = "/v1/images/generations" // Correct image generation endpoint
    
    fun validateApiKey(): Boolean {
        // AIML API doesn't have a validation endpoint, so we'll validate during image generation
        logger.info("✅ AIML API key validation will be performed during image generation")
        logger.info("📋 Skipping pre-validation (no validation endpoint available)")
        return true
    }
    
    fun generateImage(prompt: String, size: String, outputPath: String, model: String): Boolean {
        return try {
            logger.info("Sending request to AIML API...")
            
            // Parse size to width and height
            val (width, height) = parseSize(size)
            
            // Update with actual AIML API request format
            val requestBody = AimlApiRequest(
                model = model,
                prompt = prompt,
                background = "auto",
                moderation = "auto",
                n = 1,
                outputCompression = 100,
                outputFormat = "png",
                quality = "medium",
                size = size,
                responseFormat = "url"
            )
            
            val jsonBody = mapper.writeValueAsString(requestBody)
            logger.debug("Request body: $jsonBody")
            
            // Update with actual AIML API image generation endpoint
            val request = Request.Builder()
                .url("$baseUrl$imageGenerationEndpoint") // Update with actual endpoint
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
                            logger.error("Your AIML API account doesn't have enough credits.")
                            logger.error("Solutions:")
                            logger.error("1. Check your balance at: https://aimlapi.com/app/keys")
                            logger.error("2. Purchase more credits if needed")
                            logger.error("3. Try using a different API key")
                        } else if (responseBody?.contains("invalid_api_key") == true) {
                            logger.error("INVALID API KEY:")
                            logger.error("The provided API key is not valid.")
                            logger.error("Please check your API key at: https://aimlapi.com/app/keys")
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
            
            // Update with actual AIML API response format
            val imageResponse = mapper.readValue<AimlApiResponse>(responseBody!!)
            
            if (imageResponse.data.isNotEmpty()) {
                val imageUrl = imageResponse.data[0].url
                logger.info("Image URL received: $imageUrl")
                
                downloadImage(imageUrl, outputPath)
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
                logger.warn("Unknown size format: $size, using default 256x256")
                Pair(256, 256)
            }
        }
    }
    
    private fun downloadImage(imageUrl: String, outputPath: String): Boolean {
        return try {
            logger.info("Downloading image from: $imageUrl")
            
            val request = Request.Builder()
                .url(imageUrl)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                logger.error("Failed to download image: ${response.code}")
                return false
            }
            
            val imageBytes = response.body?.bytes()
            if (imageBytes != null) {
                File(outputPath).writeBytes(imageBytes)
                logger.info("✅ Image saved successfully!")
                true
            } else {
                logger.error("No image data received")
                false
            }
            
        } catch (e: IOException) {
            logger.error("Error downloading image: ${e.message}", e)
            false
        }
    }
    
    fun getAvailableModels(): List<String> {
        return listOf(
            "openai/gpt-image-1", // DALL-E 3 equivalent
            "openai/gpt-image-2", // DALL-E 2 equivalent
            "stability-ai/stable-diffusion-xl-1024-v1-0",
            "stability-ai/stable-diffusion-v1-6",
            "midjourney/midjourney-v6"
        )
    }
}

// Data classes for AIML API - Update with actual API format
data class AimlApiRequest(
    val model: String,
    val prompt: String,
    val background: String,
    val moderation: String,
    val n: Int,
    val outputCompression: Int,
    val outputFormat: String,
    val quality: String,
    val size: String,
    val responseFormat: String
)

data class AimlApiResponse(
    val created: Long,
    val data: List<AimlApiImageData>
)

data class AimlApiImageData(
    val url: String,
    @JsonProperty("revised_prompt")
    val revisedPrompt: String? = null
) 