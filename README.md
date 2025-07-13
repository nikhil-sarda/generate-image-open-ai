# Generate Image OpenAI

A Kotlin-based Maven project that generates images using OpenAI's DALL-E API based on text prompts.

## Features

- Generate images from text prompts using OpenAI's DALL-E models
- Support for different image sizes (256x256, 512x512, 1024x1024)
- Command-line interface with flexible options
- Automatic image download and saving
- Comprehensive error handling and logging
- API key validation and enhanced error messages

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API key

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

### Basic Usage

```bash
java -jar target/generate-image-openai-1.0.0.jar \
  --prompt "A beautiful sunset over mountains" \
  --api-key "your-openai-api-key-here"
```

### Advanced Usage

```bash
java -jar target/generate-image-openai-1.0.0.jar \
  --prompt "A futuristic city with flying cars" \
  --api-key "your-openai-api-key-here" \
  --size "1024x1024" \
  --output-path "my_image.png" \
  --model "dall-e-3"
```

### Command Line Options

| Option | Short | Description | Default | Required |
|--------|-------|-------------|---------|----------|
| `--prompt` | `-p` | Text prompt for image generation | - | Yes |
| `--api-key` | `-k` | OpenAI API key | - | Yes |
| `--size` | `-s` | Image size (256x256, 512x512, 1024x1024) | 1024x1024 | No |
| `--output-path` | `-o` | Output file path | generated_image.png | No |
| `--model` | `-m` | OpenAI model to use | dall-e-3 | No |

### Examples

1. **Generate a simple image:**
   ```bash
   java -jar target/generate-image-openai-1.0.0.jar \
     -p "A cute cat playing with yarn" \
     -k "sk-your-api-key-here"
   ```

2. **Generate a high-resolution image:**
   ```bash
   java -jar target/generate-image-openai-1.0.0.jar \
     -p "A majestic dragon flying over a medieval castle" \
     -k "sk-your-api-key-here" \
     -s "1024x1024" \
     -o "dragon_castle.png"
   ```

3. **Use DALL-E 2 model:**
   ```bash
   java -jar target/generate-image-openai-1.0.0.jar \
     -p "An abstract painting with vibrant colors" \
     -k "sk-your-api-key-here" \
     -m "dall-e-2" \
     -s "512x512"
   ```

## API Key Setup

1. Go to [OpenAI API](https://platform.openai.com/api-keys)
2. Create a new API key
3. Use the key with the `--api-key` parameter

**Note:** Keep your API key secure and never commit it to version control.

## Supported Models

- `dall-e-3` (default) - Latest model with highest quality
- `dall-e-2` - Previous generation model

## Supported Image Sizes

- `256x256` - Small images, faster generation
- `512x512` - Medium images, balanced quality/speed
- `1024x1024` - Large images, highest quality (default)

## Error Handling

The application includes comprehensive error handling for:
- Invalid API keys
- Network connectivity issues
- API rate limits
- Invalid prompts
- File system errors
- Billing limit issues

### Enhanced Error Messages

The application now provides specific error messages and solutions for common issues:
- **Billing Issues**: Clear guidance on resolving billing limits
- **API Key Validation**: Pre-validation of API keys before image generation
- **Network Errors**: Detailed network connectivity troubleshooting
- **Rate Limits**: Information about API rate limiting

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
4. **"Billing hard limit reached"** - Check your OpenAI billing settings and limits

### Getting Help

If you encounter issues:
1. Check the logs for detailed error messages
2. Verify your API key is correct
3. Ensure you have sufficient API credits
4. Check OpenAI's service status
5. Review the enhanced error messages for specific solutions 