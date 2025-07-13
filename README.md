# Generate Image AI

A Kotlin-based Maven project that generates images using multiple AI providers (OpenAI DALL-E and Stability AI Stable Diffusion) based on text prompts.

## Features

- Generate images from text prompts using multiple AI providers
- **OpenAI DALL-E**: High-quality image generation with DALL-E 2 and DALL-E 3
- **Stability AI Stable Diffusion**: Advanced image generation with various Stable Diffusion models
- Support for different image sizes (256x256, 512x512, 1024x1024, 1024x768, 768x1024)
- Command-line interface with flexible options
- Automatic image download and saving
- Comprehensive error handling and logging
- API key validation and enhanced error messages
- Provider-specific error handling and guidance

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- API key from your chosen provider:
  - **OpenAI API key** (for DALL-E models)
  - **Stability AI API key** (for Stable Diffusion models)

## Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd generate-image-open-ai
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Create an executable JAR:**
   ```bash
   mvn package
   ```

## Usage

### Basic Usage (Stable Diffusion - Default)

```bash
java -jar target/generate-image-openai-1.0.0.jar \
  --prompt "A beautiful sunset over mountains" \
  --api-key "your-stability-ai-api-key-here"
```

*Note: Default size is now 256x256 for maximum token efficiency*

### OpenAI DALL-E Usage

```bash
java -jar target/generate-image-openai-1.0.0.jar \
  --prompt "A futuristic city with flying cars" \
  --api-key "your-openai-api-key-here" \
  --provider "openai" \
  --size "1024x1024" \
  --output-path "my_image.png" \
  --model "dall-e-3"
```

### Advanced Stable Diffusion Usage

```bash
java -jar target/generate-image-openai-1.0.0.jar \
  --prompt "A majestic dragon flying over a medieval castle" \
  --api-key "your-stability-ai-api-key-here" \
  --provider "stable-diffusion" \
  --size "1024x1024" \
  --output-path "dragon_castle.png" \
  --model "stable-diffusion-xl-1024-v1-0"
```

### Command Line Options

| Option | Short | Description | Default | Required |
|--------|-------|-------------|---------|----------|
| `--prompt` | `-p` | Text prompt for image generation | - | Yes |
| `--api-key` | `-k` | API key for the selected provider | - | Yes |
| `--provider` | `-r` | Image generation provider (openai, stable-diffusion) | stable-diffusion | No |
| `--size` | `-s` | Image size (see supported sizes below) | 256x256 | No |
| `--output-path` | `-o` | Output file path | generated_image.png | No |
| `--model` | `-m` | Model to use (see supported models below) | auto-selected | No |

### Examples

1. **Generate with Stable Diffusion (default):**
   ```bash
   java -jar target/generate-image-openai-1.0.0.jar \
     -p "A cute cat playing with yarn" \
     -k "your-stability-ai-api-key"
   ```

2. **Generate with OpenAI DALL-E:**
   ```bash
   java -jar target/generate-image-openai-1.0.0.jar \
     -p "An abstract painting with vibrant colors" \
     -k "sk-your-openai-api-key" \
     -r "openai" \
     -m "dall-e-3"
   ```

3. **Use specific Stable Diffusion model:**
   ```bash
   java -jar target/generate-image-openai-1.0.0.jar \
     -p "A cyberpunk cityscape at night" \
     -k "your-stability-ai-api-key" \
     -r "stable-diffusion" \
     -m "stable-diffusion-xl-beta-v2-2-2" \
     -s "1024x768"
   ```

## API Key Setup

### Stability AI (Stable Diffusion) - Recommended
1. Go to [Stability AI Platform](https://platform.stability.ai/)
2. Create an account and get free credits
3. Generate an API key at [API Keys](https://platform.stability.ai/account/keys)
4. Use the key with the `--api-key` parameter

### OpenAI (DALL-E)
1. Go to [OpenAI API](https://platform.openai.com/api-keys)
2. Create a new API key
3. Use the key with the `--api-key` parameter and `--provider openai`

**Note:** Keep your API keys secure and never commit them to version control.

## Supported Models

### Stability AI (Stable Diffusion) Models
- `stable-diffusion-xl-1024-v1-0` (default) - Latest XL model, highest quality
- `stable-diffusion-xl-beta-v2-2-2` - Beta XL model with enhanced features
- `stable-diffusion-v1-6` - Standard Stable Diffusion v1.6
- `deepfloyd-if-v1-0` - DeepFloyd IF model for high-quality images
- `stable-diffusion-2-1` - Stable Diffusion 2.1
- `stable-diffusion-2-1-base` - Base version of SD 2.1

### OpenAI Models
- `dall-e-3` (default) - Latest model with highest quality
- `dall-e-2` - Previous generation model

## Supported Image Sizes

### Stability AI (Stable Diffusion)
- `256x256` - Small images, fastest generation, lowest token usage (~1-2 tokens)
- `256x256` - Small images, fastest generation, lowest token usage (~1-2 tokens) (default)
- `512x512` - Medium images, balanced quality/speed, moderate token usage (~4-6 tokens)
- `1024x1024` - Large images, highest quality, higher token usage (~8-12 tokens)
- `1024x768` - Landscape format, moderate token usage (~6-8 tokens)
- `768x1024` - Portrait format, moderate token usage (~6-8 tokens)

### OpenAI (DALL-E)
- `256x256` - Small images (DALL-E 2 only)
- `512x512` - Medium images (DALL-E 2 only)
- `1024x1024` - Large images (default)
- `1024x1792` - Portrait format (DALL-E 3 only)
- `1792x1024` - Landscape format (DALL-E 3 only)

## Provider Comparison

| Feature | Stability AI | OpenAI |
|---------|-------------|--------|
| **Free Credits** | ✅ Yes (25 free credits) | ❌ No (requires billing setup) |
| **Model Variety** | ✅ Multiple SD models | ✅ DALL-E 2 & 3 |
| **Image Quality** | ✅ High quality | ✅ Very high quality |
| **Speed** | ✅ Fast | ⚠️ Slower |
| **Cost** | ✅ Lower cost | ⚠️ Higher cost |
| **API Limits** | ✅ Generous | ⚠️ Strict |

## Error Handling

The application includes comprehensive error handling for both providers:
- Invalid API keys
- Network connectivity issues
- API rate limits
- Invalid prompts
- File system errors
- Billing/credit limit issues

### Enhanced Error Messages

The application provides specific error messages and solutions for common issues:
- **Stability AI**: Credit balance checks and purchase guidance
- **OpenAI**: Billing limit resolution and API key validation
- **Network Errors**: Detailed connectivity troubleshooting
- **Rate Limits**: Provider-specific rate limiting information

## Logging

The application uses SLF4J for logging. Log levels can be configured by setting the `org.slf4j.simpleLogger.defaultLogLevel` system property:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG \
  -jar target/generate-image-openai-1.0.0.jar \
  -p "Your prompt" \
  -k "your-api-key"
```

## Development

### Running Tests

```bash
mvn test
```

### Building from Source

```bash
mvn clean compile
```

### Running in Development Mode

```bash
mvn exec:java -Dexec.mainClass="com.example.imagegenerator.ImageGeneratorAppKt" \
  -Dexec.args="-p 'Your prompt' -k 'your-api-key'"
```

## Dependencies

- **Kotlin** - Programming language
- **OkHttp** - HTTP client for API communication
- **Jackson** - JSON processing
- **kotlinx-cli** - Command-line argument parsing
- **SLF4J** - Logging framework

## License

This project is open source and available under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Troubleshooting

### Common Issues

1. **"API request failed"** - Check your API key and internet connection
2. **"No image data received"** - The prompt might be inappropriate or the API might be rate-limited
3. **"Failed to download image"** - Check file permissions and disk space
4. **"Insufficient credits"** (Stability AI) - Check your credit balance at platform.stability.ai
5. **"Billing hard limit reached"** (OpenAI) - Check your OpenAI billing settings

### Getting Help

If you encounter issues:
1. Check the logs for detailed error messages
2. Verify your API key is correct
3. Ensure you have sufficient credits/API quota
4. Check the provider's service status
5. Review the enhanced error messages for specific solutions

### Token Usage Optimization

For users with limited tokens (like the 50,000 token limit), here are some tips:

**Token Usage by Image Size:**
- `256x256`: ~1-2 tokens per image (default, best for testing)
- `512x512`: ~4-6 tokens per image (good balance)
- `1024x1024`: ~8-12 tokens per image (highest quality)

**Maximizing Your 50,000 Tokens:**
- Use `256x256` for testing: ~25,000-50,000 images
- Use `512x512` for production: ~8,000-12,500 images
- Use `1024x1024` for high-quality: ~4,000-6,000 images

**Tips for Token Efficiency:**
- Start with `256x256` for testing prompts (default)
- Use `512x512` for production images
- Reserve `1024x1024` for final, high-quality images
- Keep prompts concise to reduce token usage

### Why Choose Stable Diffusion?

- **Free Credits**: Get 25 free credits when you sign up
- **Lower Costs**: Generally more affordable than OpenAI
- **Model Variety**: Multiple models for different use cases
- **No Billing Setup**: Start generating immediately
- **Open Source**: Based on open-source technology
- **Token Efficient**: Smaller images use fewer tokens 