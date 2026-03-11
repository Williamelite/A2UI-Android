package org.a2ui.compose.ai.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 用户行为追踪器
 */
class UserBehaviorTracker {
    private val behaviorData = mutableMapOf<String, UserBehaviorData>()
    private val interactionFlow = MutableSharedFlow<UserInteraction>()

    /**
     * 记录用户交互
     */
    fun recordInteraction(interaction: UserInteraction) {
        val userId = interaction.userId
        val userData = behaviorData.getOrPut(userId) { UserBehaviorData(userId) }

        userData.addInteraction(interaction)
        behaviorData[userId] = userData

        // 发送到流中供实时分析
        interactionFlow.tryEmit(interaction)
    }

    /**
     * 获取用户行为模式
     */
    fun getUserBehaviorPattern(userId: String): UserBehaviorPattern {
        val userData = behaviorData[userId] ?: return UserBehaviorPattern.default()
        return analyzeUserBehavior(userData)
    }

    /**
     * 获取实时交互流
     */
    fun getInteractionFlow(): Flow<UserInteraction> = interactionFlow.asSharedFlow()

    private fun analyzeUserBehavior(userData: UserBehaviorData): UserBehaviorPattern {
        val interactions = userData.interactions

        return UserBehaviorPattern(
            userId = userData.userId,
            preferredInteractionTypes = analyzePreferredInteractions(interactions),
            averageSessionDuration = calculateAverageSessionDuration(interactions),
            mostUsedComponents = findMostUsedComponents(interactions),
            preferredLayoutDensity = analyzeLayoutDensity(interactions),
            accessibilityPatterns = analyzeAccessibilityPatterns(interactions),
            deviceUsagePatterns = analyzeDeviceUsage(interactions),
            timeBasedPatterns = analyzeTimePatterns(interactions),
            learningScore = calculateLearningScore(interactions)
        )
    }

    private fun analyzePreferredInteractions(interactions: List<UserInteraction>): Map<InteractionType, Float> {
        val typeCount = interactions.groupingBy { it.type }.eachCount()
        val total = interactions.size.toFloat()

        return typeCount.mapValues { (_, count) -> count / total }
    }

    private fun calculateAverageSessionDuration(interactions: List<UserInteraction>): Long {
        if (interactions.isEmpty()) return 0L

        val sessions = groupInteractionsIntoSessions(interactions)
        return sessions.map { it.duration }.average().toLong()
    }

    private fun findMostUsedComponents(interactions: List<UserInteraction>): List<String> {
        return interactions
            .mapNotNull { it.componentId }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(10)
            .map { it.first }
    }

    private fun analyzeLayoutDensity(interactions: List<UserInteraction>): DensityPreference {
        val scrollInteractions = interactions.filter { it.type == InteractionType.SCROLL }
        val avgScrollDistance = scrollInteractions.map { it.metadata["scrollDistance"] as? Float ?: 0f }.average()

        return when {
            avgScrollDistance > 1000f -> DensityPreference.HIGH
            avgScrollDistance > 500f -> DensityPreference.MEDIUM
            else -> DensityPreference.LOW
        }
    }

    private fun analyzeAccessibilityPatterns(interactions: List<UserInteraction>): AccessibilityNeeds {
        val longPresses = interactions.count { it.type == InteractionType.LONG_PRESS }
        val totalInteractions = interactions.size
        val longPressRatio = if (totalInteractions > 0) longPresses.toFloat() / totalInteractions else 0f

        return AccessibilityNeeds(
            largeText = longPressRatio > 0.1f,
            highContrast = false, // 需要更复杂的分析
            reducedMotion = interactions.any { it.metadata["reducedMotion"] == true },
            screenReader = interactions.any { it.metadata["screenReader"] == true }
        )
    }

    private fun analyzeDeviceUsage(interactions: List<UserInteraction>): DeviceUsagePattern {
        val deviceTypes = interactions.mapNotNull { it.deviceInfo?.let { info ->
            if (info.isTablet) "tablet" else "phone"
        } }.distinct()

        val orientationChanges = interactions.count { it.type == InteractionType.ORIENTATION_CHANGE }

        return DeviceUsagePattern(
            primaryDeviceType = deviceTypes.firstOrNull() ?: "phone",
            multiDeviceUser = deviceTypes.size > 1,
            frequentOrientationChanges = orientationChanges > interactions.size * 0.1
        )
    }

    private fun analyzeTimePatterns(interactions: List<UserInteraction>): TimeBasedPattern {
        val hourlyUsage = interactions.groupBy {
            java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).hour
        }.mapValues { it.value.size }

        val peakHour = hourlyUsage.maxByOrNull { it.value }?.key ?: 12

        return TimeBasedPattern(
            peakUsageHour = peakHour,
            weekdayUsage = calculateWeekdayUsage(interactions),
            sessionFrequency = calculateSessionFrequency(interactions)
        )
    }

    private fun calculateLearningScore(interactions: List<UserInteraction>): Float {
        if (interactions.size < 10) return 0f

        val recentInteractions = interactions.takeLast(50)
        val olderInteractions = interactions.dropLast(50).takeLast(50)

        val recentEfficiency = calculateInteractionEfficiency(recentInteractions)
        val olderEfficiency = calculateInteractionEfficiency(olderInteractions)

        return ((recentEfficiency - olderEfficiency) / olderEfficiency).coerceIn(-1f, 1f)
    }

    private fun calculateInteractionEfficiency(interactions: List<UserInteraction>): Float {
        if (interactions.isEmpty()) return 0f

        val successfulInteractions = interactions.count { it.successful }
        return successfulInteractions.toFloat() / interactions.size
    }

    private fun groupInteractionsIntoSessions(interactions: List<UserInteraction>): List<UserSession> {
        val sessions = mutableListOf<UserSession>()
        var currentSession: MutableList<UserInteraction> = mutableListOf()
        var lastTimestamp = 0L

        interactions.sortedBy { it.timestamp }.forEach { interaction ->
            if (lastTimestamp == 0L || interaction.timestamp - lastTimestamp < 30_000) { // 30秒间隔
                currentSession.add(interaction)
            } else {
                if (currentSession.isNotEmpty()) {
                    sessions.add(createSession(currentSession))
                    currentSession = mutableListOf(interaction)
                }
            }
            lastTimestamp = interaction.timestamp
        }

        if (currentSession.isNotEmpty()) {
            sessions.add(createSession(currentSession))
        }

        return sessions
    }

    private fun createSession(interactions: List<UserInteraction>): UserSession {
        val startTime = interactions.first().timestamp
        val endTime = interactions.last().timestamp

        return UserSession(
            startTime = startTime,
            endTime = endTime,
            duration = endTime - startTime,
            interactions = interactions,
            successful = interactions.all { it.successful }
        )
    }

    private fun calculateWeekdayUsage(interactions: List<UserInteraction>): Map<Int, Int> {
        return interactions.groupBy {
            java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .dayOfWeek.value
        }.mapValues { it.value.size }
    }

    private fun calculateSessionFrequency(interactions: List<UserInteraction>): Float {
        val sessions = groupInteractionsIntoSessions(interactions)
        if (sessions.isEmpty()) return 0f

        val timeSpan = sessions.last().endTime - sessions.first().startTime
        val days = timeSpan / (24 * 60 * 60 * 1000f)

        return if (days > 0) sessions.size / days else 0f
    }
}

/**
 * 用户交互数据
 */
data class UserInteraction(
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: InteractionType,
    val componentId: String? = null,
    val componentType: String? = null,
    val position: Pair<Float, Float>? = null,
    val duration: Long = 0L,
    val successful: Boolean = true,
    val deviceInfo: DeviceInfo? = null,
    val metadata: Map<String, Any> = emptyMap()
)

enum class InteractionType {
    TAP, LONG_PRESS, SWIPE, SCROLL, PINCH, DRAG,
    ORIENTATION_CHANGE, FOCUS, BLUR, HOVER, KEY_PRESS
}

/**
 * 用户行为数据
 */
data class UserBehaviorData(
    val userId: String,
    val interactions: MutableList<UserInteraction> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun addInteraction(interaction: UserInteraction) {
        interactions.add(interaction)
        // 保持最近1000个交互记录
        if (interactions.size > 1000) {
            interactions.removeAt(0)
        }
    }
}

/**
 * 用户行为模式
 */
data class UserBehaviorPattern(
    val userId: String,
    val preferredInteractionTypes: Map<InteractionType, Float>,
    val averageSessionDuration: Long,
    val mostUsedComponents: List<String>,
    val preferredLayoutDensity: DensityPreference,
    val accessibilityPatterns: AccessibilityNeeds,
    val deviceUsagePatterns: DeviceUsagePattern,
    val timeBasedPatterns: TimeBasedPattern,
    val learningScore: Float
) {
    companion object {
        fun default() = UserBehaviorPattern(
            userId = "",
            preferredInteractionTypes = emptyMap(),
            averageSessionDuration = 0L,
            mostUsedComponents = emptyList(),
            preferredLayoutDensity = DensityPreference.MEDIUM,
            accessibilityPatterns = AccessibilityNeeds(),
            deviceUsagePatterns = DeviceUsagePattern(),
            timeBasedPatterns = TimeBasedPattern(),
            learningScore = 0f
        )
    }
}

data class DeviceUsagePattern(
    val primaryDeviceType: String = "phone",
    val multiDeviceUser: Boolean = false,
    val frequentOrientationChanges: Boolean = false
)

data class TimeBasedPattern(
    val peakUsageHour: Int = 12,
    val weekdayUsage: Map<Int, Int> = emptyMap(),
    val sessionFrequency: Float = 0f
)

data class UserSession(
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val interactions: List<UserInteraction>,
    val successful: Boolean
)

/**
 * 布局推荐器
 */
class LayoutRecommender {
    private val mlModel = LayoutMLModel()

    /**
     * 生成布局推荐
     */
    fun generateRecommendations(
        currentLayout: LayoutConfiguration,
        performance: LayoutPerformance,
        userBehavior: UserBehaviorPattern,
        context: LayoutContext
    ): List<LayoutRecommendation> {
        val recommendations = mutableListOf<LayoutRecommendation>()

        // 1. 性能优化推荐
        if (performance.renderComplexity > 0.7f) {
            recommendations.add(generatePerformanceRecommendation(currentLayout, performance))
        }

        // 2. 用户体验优化推荐
        recommendations.add(generateUXRecommendation(currentLayout, userBehavior, context))

        // 3. 响应式布局推荐
        if (performance.responsiveness < 0.6f) {
            recommendations.add(generateResponsiveRecommendation(currentLayout, context))
        }

        // 4. 可访问性推荐
        if (performance.accessibility < 0.7f) {
            recommendations.add(generateAccessibilityRecommendation(currentLayout, context))
        }

        // 5. ML模型推荐
        recommendations.add(mlModel.generateRecommendation(currentLayout, userBehavior, context))

        return recommendations.sortedByDescending { it.confidence }
    }

    private fun generatePerformanceRecommendation(
        layout: LayoutConfiguration,
        performance: LayoutPerformance
    ): LayoutRecommendation {
        val optimizedLayout = layout.copy(
            containerType = when (layout.containerType) {
                ContainerType.FLOW -> ContainerType.GRID
                ContainerType.ADAPTIVE -> ContainerType.COLUMN
                else -> layout.containerType
            },
            components = layout.components.take(20) // 限制组件数量
        )

        return LayoutRecommendation(
            layout = optimizedLayout,
            improvements = LayoutImprovements(
                performanceGain = 0.3f,
                uxScore = 0.1f,
                accessibilityScore = 0f,
                responsiveScore = 0f,
                specificImprovements = listOf(
                    "减少渲染复杂度",
                    "优化容器类型",
                    "限制组件数量"
                )
            ),
            confidence = 0.8f,
            reasoning = "通过简化布局结构和减少组件数量来提升渲染性能",
            category = RecommendationCategory.PERFORMANCE
        )
    }

    private fun generateUXRecommendation(
        layout: LayoutConfiguration,
        userBehavior: UserBehaviorPattern,
        context: LayoutContext
    ): LayoutRecommendation {
        val spacing = when (userBehavior.preferredLayoutDensity) {
            DensityPreference.LOW -> 16.dp
            DensityPreference.MEDIUM -> 12.dp
            DensityPreference.HIGH -> 8.dp
        }

        val optimizedLayout = layout.copy(
            spacing = spacing,
            arrangement = if (userBehavior.mostUsedComponents.isNotEmpty()) {
                LayoutArrangement.TOP_TO_BOTTOM // 重要组件优先
            } else {
                layout.arrangement
            }
        )

        return LayoutRecommendation(
            layout = optimizedLayout,
            improvements = LayoutImprovements(
                performanceGain = 0f,
                uxScore = 0.4f,
                accessibilityScore = 0.1f,
                responsiveScore = 0f,
                specificImprovements = listOf(
                    "调整间距以匹配用户偏好",
                    "优化组件排列顺序",
                    "提升交互便利性"
                )
            ),
            confidence = 0.7f,
            reasoning = "基于用户行为模式优化布局间距和组件排列",
            category = RecommendationCategory.UX
        )
    }

    private fun generateResponsiveRecommendation(
        layout: LayoutConfiguration,
        context: LayoutContext
    ): LayoutRecommendation {
        val responsiveConfig = ResponsiveConfig(
            breakpoints = mapOf(
                ScreenSize.COMPACT to layout.copy(
                    containerType = ContainerType.COLUMN,
                    spacing = 8.dp
                ),
                ScreenSize.MEDIUM to layout.copy(
                    containerType = ContainerType.GRID,
                    spacing = 12.dp
                ),
                ScreenSize.EXPANDED to layout.copy(
                    containerType = ContainerType.ROW,
                    spacing = 16.dp
                )
            ),
            adaptiveSpacing = true,
            adaptiveSizing = true
        )

        val optimizedLayout = layout.copy(responsive = responsiveConfig)

        return LayoutRecommendation(
            layout = optimizedLayout,
            improvements = LayoutImprovements(
                performanceGain = 0f,
                uxScore = 0.2f,
                accessibilityScore = 0f,
                responsiveScore = 0.6f,
                specificImprovements = listOf(
                    "添加响应式断点",
                    "自适应间距调整",
                    "多屏幕尺寸优化"
                )
            ),
            confidence = 0.9f,
            reasoning = "添加响应式配置以适应不同屏幕尺寸",
            category = RecommendationCategory.RESPONSIVE
        )
    }

    private fun generateAccessibilityRecommendation(
        layout: LayoutConfiguration,
        context: LayoutContext
    ): LayoutRecommendation {
        val accessibilityNeeds = context.userPreferences.accessibilityNeeds

        val optimizedSpacing = if (accessibilityNeeds.largeText) {
            maxOf(layout.spacing, 12.dp)
        } else {
            layout.spacing
        }

        val optimizedLayout = layout.copy(
            spacing = optimizedSpacing,
            padding = PaddingValues(
                horizontal = maxOf(layout.padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr), 16.dp),
                vertical = maxOf(layout.padding.calculateTopPadding(), 12.dp)
            )
        )

        return LayoutRecommendation(
            layout = optimizedLayout,
            improvements = LayoutImprovements(
                performanceGain = 0f,
                uxScore = 0.1f,
                accessibilityScore = 0.5f,
                responsiveScore = 0f,
                specificImprovements = listOf(
                    "增加触摸目标大小",
                    "优化间距以支持大文本",
                    "提升可访问性"
                )
            ),
            confidence = 0.8f,
            reasoning = "根据用户可访问性需求调整布局参数",
            category = RecommendationCategory.ACCESSIBILITY
        )
    }
}

/**
 * 布局机器学习模型（简化实现）
 */
class LayoutMLModel {
    fun generateRecommendation(
        layout: LayoutConfiguration,
        userBehavior: UserBehaviorPattern,
        context: LayoutContext
    ): LayoutRecommendation {
        // 简化的ML推荐逻辑
        val features = extractFeatures(layout, userBehavior, context)
        val prediction = predict(features)

        return LayoutRecommendation(
            layout = generateOptimizedLayout(layout, prediction),
            improvements = LayoutImprovements(
                performanceGain = prediction.performanceGain,
                uxScore = prediction.uxScore,
                accessibilityScore = prediction.accessibilityScore,
                responsiveScore = prediction.responsiveScore,
                specificImprovements = prediction.improvements
            ),
            confidence = prediction.confidence,
            reasoning = "基于机器学习模型的综合优化建议",
            category = RecommendationCategory.CONTENT_OPTIMIZATION
        )
    }

    private fun extractFeatures(
        layout: LayoutConfiguration,
        userBehavior: UserBehaviorPattern,
        context: LayoutContext
    ): MLFeatures {
        return MLFeatures(
            componentCount = layout.components.size,
            containerType = layout.containerType.ordinal,
            screenSize = context.screenSize.ordinal,
            userLearningScore = userBehavior.learningScore,
            sessionDuration = userBehavior.averageSessionDuration,
            deviceType = if (context.deviceInfo.isTablet) 1 else 0
        )
    }

    private fun predict(features: MLFeatures): MLPrediction {
        // 简化的预测逻辑（实际应该使用训练好的模型）
        val score = Random.nextFloat()

        return MLPrediction(
            performanceGain = score * 0.3f,
            uxScore = score * 0.4f,
            accessibilityScore = score * 0.2f,
            responsiveScore = score * 0.3f,
            confidence = score,
            improvements = listOf("ML优化建议")
        )
    }

    private fun generateOptimizedLayout(
        layout: LayoutConfiguration,
        prediction: MLPrediction
    ): LayoutConfiguration {
        // 基于预测结果生成优化布局
        return layout.copy(
            spacing = layout.spacing * (1f + prediction.performanceGain)
        )
    }
}

data class MLFeatures(
    val componentCount: Int,
    val containerType: Int,
    val screenSize: Int,
    val userLearningScore: Float,
    val sessionDuration: Long,
    val deviceType: Int
)

data class MLPrediction(
    val performanceGain: Float,
    val uxScore: Float,
    val accessibilityScore: Float,
    val responsiveScore: Float,
    val confidence: Float,
    val improvements: List<String>
)