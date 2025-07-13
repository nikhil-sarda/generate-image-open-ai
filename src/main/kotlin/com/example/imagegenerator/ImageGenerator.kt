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
    
    fun validateApiKey(): Boolean {
        return try {
            logger.info("Validating API key...")
            
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                logger.info("✅ API key is valid and has proper permissions")
                true
            } else {
                logger.error("❌ API key validation failed with code: ${response.code}")
                val responseBody = response.body?.string()
                logger.error("Response: $responseBody")
                false
            }
            
        } catch (e: Exception) {
            logger.error("❌ Error validating API key: ${e.message}")
            false
        }
    }
    
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
                logger.error("Response headers: ${response.headers}")
                val responseBody = response.body?.string()
                logger.error("Response body: $responseBody")
                
                // Provide specific guidance for common errors
                when (response.code) {
                    400 -> {
                        if (responseBody?.contains("billing_hard_limit_reached") == true) {
                            logger.error("BILLING ISSUE DETECTED:")
                            logger.error("Your OpenAI account has reached its billing limit.")
                            logger.error("Solutions:")
                            logger.error("1. Check your billing at: https://platform.openai.com/account/billing")
                            logger.error("2. Add payment method if needed")
                            logger.error("3. Increase spending limits")
                            logger.error("4. Try using a different API key")
                        } else if (responseBody?.contains("invalid_api_key") == true) {
                            logger.error("INVALID API KEY:")
                            logger.error("The provided API key is not valid.")
                            logger.error("Please check your API key at: https://platform.openai.com/api-keys")
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
    
    fun analyzeApiKey(): Map<String, Any> {
        val analysis = mutableMapOf<String, Any>()
        
        try {
            logger.info("🔍 Analyzing API key...")
            
            // Check key format
            analysis["key_format"] = when {
                apiKey.startsWith("sk-") -> "Personal API Key"
                apiKey.startsWith("sk-proj-") -> "Project API Key"
                apiKey.startsWith("sk-svcacct-") -> "Service Account API Key"
                apiKey.startsWith("sk-org-") -> "Organization API Key"
                else -> "Unknown Format"
            }
            
            // Get organization info
            val orgRequest = Request.Builder()
                .url("$baseUrl/organizations")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            
            val orgResponse = client.newCall(orgRequest).execute()
            if (orgResponse.isSuccessful) {
                val orgBody = orgResponse.body?.string()
                analysis["organization_info"] = orgBody ?: "No organization info"
                logger.info("📋 Organization info retrieved")
            } else {
                analysis["organization_info"] = "Failed to get organization info: ${orgResponse.code}"
            }
            
            // Get models info
            val modelsRequest = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            
            val modelsResponse = client.newCall(modelsRequest).execute()
            if (modelsResponse.isSuccessful) {
                val modelsBody = modelsResponse.body?.string()
                analysis["models_info"] = modelsBody ?: "No models info"
                logger.info("🤖 Models info retrieved")
            } else {
                analysis["models_info"] = "Failed to get models info: ${modelsResponse.code}"
            }
            
            // Get billing info
            val billingRequest = Request.Builder()
                .url("$baseUrl/dashboard/billing/subscription")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            
            val billingResponse = client.newCall(billingRequest).execute()
            if (billingResponse.isSuccessful) {
                val billingBody = billingResponse.body?.string()
                analysis["billing_info"] = billingBody ?: "No billing info"
                logger.info("💰 Billing info retrieved")
            } else {
                analysis["billing_info"] = "Failed to get billing info: ${billingResponse.code}"
            }
            
            // Test image generation endpoint specifically
            val testImageRequest = Request.Builder()
                .url("$baseUrl/images/generations")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create("application/json".toMediaType(), 
                    """{"model":"dall-e-3","prompt":"test","n":1,"size":"256x256"}"""))
                .build()
            
            val testImageResponse = client.newCall(testImageRequest).execute()
            analysis["image_generation_test"] = mapOf(
                "status_code" to testImageResponse.code,
                "response_body" to (testImageResponse.body?.string() ?: "No response body"),
                "headers" to testImageResponse.headers.toString()
            )
            
            logger.info("✅ API key analysis completed")
            
        } catch (e: Exception) {
            analysis["error"] = "Analysis failed: ${e.message}"
            logger.error("❌ API key analysis failed: ${e.message}")
        }
        
        return analysis
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