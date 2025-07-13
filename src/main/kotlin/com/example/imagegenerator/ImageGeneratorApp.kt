package com.example.imagegenerator

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlinx.cli.default
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("ImageGeneratorApp")
    
    val parser = ArgParser("image-generator")
    val prompt by parser.option(ArgType.String, shortName = "p", description = "Text prompt for image generation").required()
    val `api-key` by parser.option(ArgType.String, shortName = "k", description = "API key for the selected provider").required()
    val provider by parser.option(ArgType.String, shortName = "r", description = "Image generation provider (openai or stable-diffusion)").default("stable-diffusion")
    val size by parser.option(ArgType.String, shortName = "s", description = "Image size (256x256, 512x512, 1024x1024, 1024x768, 768x1024)").default("1024x1024")
    val `output-path` by parser.option(ArgType.String, shortName = "o", description = "Output file path").default("generated_image.png")
    val model by parser.option(ArgType.String, shortName = "m", description = "Model to use (see README for available models)").default("")
    
    try {
        parser.parse(args)
        
        logger.info("Starting image generation with prompt: $prompt")
        logger.info("Using provider: $provider, size: $size")
        
        val success = when (provider.lowercase()) {
            "openai", "dall-e" -> {
                val openaiModel = if (model.isBlank()) "dall-e-3" else model
                logger.info("Using OpenAI model: $openaiModel")
                
                val imageGenerator = ImageGenerator(`api-key`)
                
                // Validate API key first
                if (!imageGenerator.validateApiKey()) {
                    logger.error("OpenAI API key validation failed. Please check your API key and try again.")
                    System.exit(1)
                }
                
                imageGenerator.generateImage(prompt, size, `output-path`, openaiModel)
            }
            
            "stable-diffusion", "stability", "sd" -> {
                val sdModel = if (model.isBlank()) "stable-diffusion-xl-1024-v1-0" else model
                logger.info("Using Stable Diffusion model: $sdModel")
                
                val stableDiffusionGenerator = StableDiffusionGenerator(`api-key`)
                
                // Validate API key first
                if (!stableDiffusionGenerator.validateApiKey()) {
                    logger.error("Stability AI API key validation failed. Please check your API key and try again.")
                    System.exit(1)
                }
                
                stableDiffusionGenerator.generateImage(prompt, size, `output-path`, sdModel)
            }
            
            else -> {
                logger.error("Unknown provider: $provider")
                logger.error("Supported providers: openai, stable-diffusion")
                System.exit(1)
                false
            }
        }
        
        if (success) {
            logger.info("Image generated successfully! Saved to: $`output-path`")
        } else {
            logger.error("Failed to generate image")
            System.exit(1)
        }
        
    } catch (e: Exception) {
        logger.error("Error: ${e.message}")
        System.exit(1)
    }
} 