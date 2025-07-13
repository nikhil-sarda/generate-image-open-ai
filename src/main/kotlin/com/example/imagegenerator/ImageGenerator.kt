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

class ImageGenerator(private val apiKey: String) {
    private val logger = LoggerFactory.getLogger(ImageGenerator::class.java)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val mapper = jacksonObjectMapper()
    private val baseUrl = "https://api.openai.com/v1"
    
    fun generateImage(prompt: String, size: String, outputPath: String, model: String): Boolean {
        return try {
            logger.info("Sending request to OpenAI API...")
            
            val requestBody = ImageGenerationRequest(
                model = model,
                prompt = prompt,
                n = 1,
                size = size
            )
            
            val jsonBody = mapper.writeValueAsString(requestBody)
            logger.debug("Request body: $jsonBody")
            
            val request = Request.Builder()
                .url("$baseUrl/images/generations")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create("application/json".toMediaType(), jsonBody))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                logger.error("API request failed with code: ${response.code}")
                logger.error("Response body: ${response.body?.string()}")
                return false
            }
            
            val responseBody = response.body?.string()
            logger.debug("Response body: $responseBody")
            
            val imageResponse = mapper.readValue<ImageGenerationResponse>(responseBody!!)
            
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
                logger.info("Image saved to: $outputPath")
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
}

// Data classes for API requests and responses
data class ImageGenerationRequest(
    val model: String,
    val prompt: String,
    val n: Int,
    val size: String
)

data class ImageGenerationResponse(
    val created: Long,
    val data: List<ImageData>
)

data class ImageData(
    val url: String,
    @JsonProperty("revised_prompt")
    val revisedPrompt: String? = null
) 