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
    val `api-key` by parser.option(ArgType.String, shortName = "k", description = "OpenAI API key").required()
    val size by parser.option(ArgType.String, shortName = "s", description = "Image size (256x256, 512x512, 1024x1024)").default("1024x1024")
    val `output-path` by parser.option(ArgType.String, shortName = "o", description = "Output file path").default("generated_image.png")
    val model by parser.option(ArgType.String, shortName = "m", description = "OpenAI model to use").default("dall-e-3")
    
    try {
        parser.parse(args)
        
        logger.info("Starting image generation with prompt: $prompt")
        logger.info("Using model: $model, size: $size")
        
        val imageGenerator = ImageGenerator(`api-key`)
        val success = imageGenerator.generateImage(prompt, size, `output-path`, model)
        
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