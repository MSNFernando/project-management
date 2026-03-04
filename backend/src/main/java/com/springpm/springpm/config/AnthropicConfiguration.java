package com.springpm.springpm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI Anthropic integration.
 * 
 * Anthropic API is optional and only enabled when ANTHROPIC_API_KEY is set in environment.
 * This prevents application startup failures when the API key is not configured.
 * 
 * To enable AI features:
 * 1. Get your API key from: https://console.anthropic.com/
 * 2. Set environment variable: ANTHROPIC_API_KEY=your-key
 * 3. Or add to .env file: ANTHROPIC_API_KEY=your-key
 * 4. When enabled, the Anthropic chat model will be available for dependency injection
 * 
 * By default, Anthropic auto-configuration is excluded in application.properties.
 * This class serves as a marker to enable it conditionally in the future if needed.
 */
@Configuration
@ConditionalOnProperty(
    name = "spring.ai.anthropic.api.key",
    matchIfMissing = false
)
public class AnthropicConfiguration {
    // This class is intentionally minimal.
    // Spring AI's auto-configuration handles bean creation when the property is set.
    // The @ConditionalOnProperty ensures this configuration is only loaded
    // when spring.ai.anthropic.api.key is explicitly set to a non-empty value.
}
