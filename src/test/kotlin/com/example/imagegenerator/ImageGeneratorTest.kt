package com.example.imagegenerator

import kotlin.test.Test
import kotlin.test.assertTrue

class ImageGeneratorTest {
    
    @Test
    fun testImageGeneratorCreation() {
        val apiKey = "test-api-key"
        val generator = ImageGenerator(apiKey)
        assertTrue(generator is ImageGenerator)
    }
    
    @Test
    fun testDataClasses() {
        val request = ImageGenerationRequest(
            model = "dall-e-3",
            prompt = "test prompt",
            n = 1,
            size = "1024x1024"
        )
        
        assertTrue(request.model == "dall-e-3")
        assertTrue(request.prompt == "test prompt")
        assertTrue(request.n == 1)
        assertTrue(request.size == "1024x1024")
    }
} 