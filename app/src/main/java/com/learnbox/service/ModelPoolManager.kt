package com.learnbox.service

import android.content.Context
import com.learnbox.data.model.ApiConfig

class ModelPoolManager(context: Context) {

    val registry = ProviderRegistry(context)

    fun getVisionConfig(): ApiConfig? {
        val (provider, model) = registry.getNextVisionModel() ?: return null
        return ApiConfig(
            name = "${provider.name} Vision",
            baseUrl = provider.baseUrl,
            apiKey = provider.apiKey,
            model = model,
            maxTokens = 2000,
            temperature = 0.7,
            isDefault = true
        )
    }

    fun getTextConfig(): ApiConfig? {
        val (provider, model) = registry.getNextTextModel() ?: return null
        return ApiConfig(
            name = "${provider.name} Text",
            baseUrl = provider.baseUrl,
            apiKey = provider.apiKey,
            model = model,
            maxTokens = 2000,
            temperature = 0.7,
            isDefault = true
        )
    }

    fun recordVisionSuccess(config: ApiConfig) {
        val providerId = findProviderId(config.baseUrl)
        registry.recordSuccess(providerId, config.model)
    }

    fun recordVisionFailure(config: ApiConfig) {
        val providerId = findProviderId(config.baseUrl)
        registry.recordFailure(providerId, config.model)
    }

    fun recordTextSuccess(config: ApiConfig) {
        val providerId = findProviderId(config.baseUrl)
        registry.recordSuccess(providerId, config.model)
    }

    fun recordTextFailure(config: ApiConfig) {
        val providerId = findProviderId(config.baseUrl)
        registry.recordFailure(providerId, config.model)
    }

    fun resetFailures() {
        registry.resetAllCooldowns()
    }

    fun getStatusSummary(): String {
        val (vision, text) = registry.getTotalModelCount()
        val providers = registry.getActiveProviders().size
        return "$providers \u4f9b\u5e94\u5546 | $vision \u89c6\u89c9\u6a21\u578b | $text \u6587\u672c\u6a21\u578b"
    }

    private fun findProviderId(baseUrl: String): String {
        return registry.getActiveProviders().firstOrNull { it.baseUrl == baseUrl }?.id ?: "unknown"
    }
}
