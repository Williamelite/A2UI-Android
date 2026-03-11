package org.a2ui.compose.ai.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*

/**
 * 自动响应式布局管理器
 */
class AutoResponsiveLayoutManager {
    private val layoutOptimizer = AILayoutOptimizer()
    private val behaviorTracker = UserBehaviorTracker()
    private var currentLayout by mutableStateOf<LayoutConfiguration?>(null)
    private var optimizationJob: Job? = null

    /**
     * 自动响应式布局组合函数
     */
    @Composable
    fun AutoResponsiveLayout(
        initialLayout: LayoutConfiguration,
        context: LayoutContext,
        modifier: Modifier = Modifier,
        onLayoutChanged: (LayoutConfiguration) -> Unit = {},
        content: @Composable BoxScope.(LayoutConfiguration) -> Unit
    ) {
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current

        // 监听屏幕配置变化
        LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp, configuration.orientation) {
            val updatedContext = context.copy(
                deviceInfo = context.deviceInfo.copy(
                    screenWidth = configuration.screenWidthDp,
                    screenHeight = configuration.screenHeightDp,
                    orientation = if (configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                        Orientation.LANDSCAPE
                    } else {
                        Orientation.PORTRAIT
                    }
                ),
                screenSize = determineScreenSize(configuration.screenWidthDp, configuration.screenHeightDp)
            )

            optimizeLayoutForContext(initialLayout, updatedContext, onLayoutChanged)
        }

        // 渲染当前布局
        val layoutToRender = currentLayout ?: initialLayout

        Box(modifier = modifier) {
            content(layoutToRender)
        }
    }

    private suspend fun optimizeLayoutForContext(
        layout: LayoutConfiguration,
        context: LayoutContext,
        onLayoutChanged: (LayoutConfiguration) -> Unit
    ) {
        optimizationJob?.cancel()
        optimizationJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val optimizationResult = layoutOptimizer.optimizeLayout(layout, context)

                withContext(Dispatchers.Main) {
                    currentLayout = optimizationResult.optimizedLayout
                    onLayoutChanged(optimizationResult.optimizedLayout)
                }
            } catch (e: Exception) {
                // 优化失败时保持原布局
                withContext(Dispatchers.Main) {
                    currentLayout = layout
                }
            }
        }
    }

    private fun determineScreenSize(widthDp: Int, heightDp: Int): ScreenSize {
        val minDimension = minOf(widthDp, heightDp)
        return when {
            minDimension < 600 -> ScreenSize.COMPACT
            minDimension < 840 -> ScreenSize.MEDIUM
            else -> ScreenSize.EXPANDED
        }
    }
}

/**
 * 响应式布局适配器
 */
class ResponsiveLayoutAdapter {
    /**
     * 根据屏幕尺寸适配布局
     */
    fun adaptLayoutForScreen(
        layout: LayoutConfiguration,
        screenSize: ScreenSize,
        deviceInfo: DeviceInfo
    ): LayoutConfiguration {
        return when (screenSize) {
            ScreenSize.COMPACT -> adaptForCompactScreen(layout, deviceInfo)
            ScreenSize.MEDIUM -> adaptForMediumScreen(layout, deviceInfo)
            ScreenSize.EXPANDED -> adaptForExpandedScreen(layout, deviceInfo)
        }
    }

    private fun adaptForCompactScreen(
        layout: LayoutConfiguration,
        deviceInfo: DeviceInfo
    ): LayoutConfiguration {
        return layout.copy(
            containerType = ContainerType.COLUMN, // 垂直布局更适合小屏
            spacing = 8.dp, // 紧凑间距
            padding = PaddingValues(12.dp), // 减少内边距
            components = layout.components.map { component ->
                component.copy(
                    size = component.size.copy(
                        width = SizeSpec.FillMax, // 充满宽度
                        height = when (component.size.height) {
                            is SizeSpec.Fixed -> SizeSpec.Fixed(
                                minOf((component.size.height as SizeSpec.Fixed).value, 200.dp)
                            )
                            else -> component.size.height
                        }
                    )
                )
            }
        )
    }

    private fun adaptForMediumScreen(
        layout: LayoutConfiguration,
        deviceInfo: DeviceInfo
    ): LayoutConfiguration {
        return layout.copy(
            containerType = if (deviceInfo.orientation == Orientation.LANDSCAPE) {
                ContainerType.ROW // 横屏时使用水平布局
            } else {
                ContainerType.GRID // 竖屏时使用网格布局
            },
            spacing = 12.dp,
            padding = PaddingValues(16.dp),
            components = adaptComponentsForMediumScreen(layout.components, deviceInfo)
        )
    }

    private fun adaptForExpandedScreen(
        layout: LayoutConfiguration,
        deviceInfo: DeviceInfo
    ): LayoutConfiguration {
        return layout.copy(
            containerType = ContainerType.ADAPTIVE, // 自适应布局
            spacing = 16.dp,
            padding = PaddingValues(24.dp),
            components = adaptComponentsForExpandedScreen(layout.components, deviceInfo)
        )
    }

    private fun adaptComponentsForMediumScreen(
        components: List<ComponentLayout>,
        deviceInfo: DeviceInfo
    ): List<ComponentLayout> {
        return components.mapIndexed { index, component ->
            component.copy(
                size = component.size.copy(
                    width = if (deviceInfo.orientation == Orientation.LANDSCAPE) {
                        SizeSpec.Fraction(0.5f) // 横屏时每个组件占50%宽度
                    } else {
                        SizeSpec.FillMax
                    }
                ),
                position = component.position.copy(
                    row = if (deviceInfo.orientation == Orientation.LANDSCAPE) 0 else index / 2,
                    column = if (deviceInfo.orientation == Orientation.LANDSCAPE) index else index % 2
                )
            )
        }
    }

    private fun adaptComponentsForExpandedScreen(
        components: List<ComponentLayout>,
        deviceInfo: DeviceInfo
    ): List<ComponentLayout> {
        return components.mapIndexed { index, component ->
            component.copy(
                size = component.size.copy(
                    width = SizeSpec.Fraction(1f / 3f), // 大屏幕时每行3个组件
                    height = component.size.height
                ),
                position = component.position.copy(
                    row = index / 3,
                    column = index % 3
                )
            )
        }
    }
}

/**
 * 布局性能评估器
 */
class LayoutPerformanceEvaluator {
    /**
     * 评估布局性能
     */
    fun evaluateLayout(
        layout: LayoutConfiguration,
        context: LayoutContext
    ): LayoutPerformanceScore {
        val renderingScore = evaluateRenderingPerformance(layout)
        val memoryScore = evaluateMemoryUsage(layout)
        val responsiveScore = evaluateResponsiveness(layout, context)
        val accessibilityScore = evaluateAccessibility(layout, context)
        val uxScore = evaluateUserExperience(layout, context)

        return LayoutPerformanceScore(
            rendering = renderingScore,
            memory = memoryScore,
            responsive = responsiveScore,
            accessibility = accessibilityScore,
            userExperience = uxScore,
            overall = calculateOverallScore(renderingScore, memoryScore, responsiveScore, accessibilityScore, uxScore)
        )
    }

    private fun evaluateRenderingPerformance(layout: LayoutConfiguration): Float {
        var score = 1.0f

        // 组件数量影响
        val componentCount = layout.components.size
        score -= when {
            componentCount > 50 -> 0.4f
            componentCount > 30 -> 0.2f
            componentCount > 20 -> 0.1f
            else -> 0f
        }

        // 容器复杂度影响
        score -= when (layout.containerType) {
            ContainerType.ADAPTIVE -> 0.3f
            ContainerType.FLOW -> 0.2f
            ContainerType.GRID -> 0.1f
            else -> 0f
        }

        // 嵌套深度影响
        val nestingDepth = calculateNestingDepth(layout)
        score -= nestingDepth * 0.05f

        return score.coerceIn(0f, 1f)
    }

    private fun evaluateMemoryUsage(layout: LayoutConfiguration): Float {
        var memoryScore = 1.0f

        layout.components.forEach { component ->
            memoryScore -= when (component.type) {
                "Image", "Video", "Canvas" -> 0.1f
                "Chart", "Graph" -> 0.05f
                "List", "Grid" -> 0.03f
                else -> 0.01f
            }
        }

        return memoryScore.coerceIn(0f, 1f)
    }

    private fun evaluateResponsiveness(layout: LayoutConfiguration, context: LayoutContext): Float {
        var score = 0.5f // 基础分

        // 检查是否有响应式配置
        if (layout.responsive != null) {
            score += 0.3f

            // 检查断点配置完整性
            val breakpoints = layout.responsive.breakpoints
            if (breakpoints.containsKey(ScreenSize.COMPACT)) score += 0.1f
            if (breakpoints.containsKey(ScreenSize.MEDIUM)) score += 0.05f
            if (breakpoints.containsKey(ScreenSize.EXPANDED)) score += 0.05f
        }

        // 检查组件大小配置
        val flexibleComponents = layout.components.count { component ->
            component.size.width is SizeSpec.Fraction || component.size.width is SizeSpec.FillMax
        }
        score += (flexibleComponents.toFloat() / layout.components.size) * 0.2f

        return score.coerceIn(0f, 1f)
    }

    private fun evaluateAccessibility(layout: LayoutConfiguration, context: LayoutContext): Float {
        var score = 0.3f // 基础分

        // 检查间距是否足够
        if (layout.spacing >= 8.dp) score += 0.2f
        if (layout.spacing >= 12.dp) score += 0.1f

        // 检查内边距
        val minPadding = minOf(
            layout.padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            layout.padding.calculateTopPadding()
        )
        if (minPadding >= 12.dp) score += 0.1f
        if (minPadding >= 16.dp) score += 0.1f

        // 检查用户偏好适配
        val userNeeds = context.userPreferences.accessibilityNeeds
        if (userNeeds.largeText && layout.spacing >= 12.dp) score += 0.1f
        if (userNeeds.highContrast) score += 0.1f // 需要更详细的检查

        return score.coerceIn(0f, 1f)
    }

    private fun evaluateUserExperience(layout: LayoutConfiguration, context: LayoutContext): Float {
        var score = 0.5f // 基础分

        // 检查布局类型是否适合内容
        score += when (context.contentType) {
            ContentType.DASHBOARD -> if (layout.containerType == ContainerType.GRID) 0.2f else 0f
            ContentType.LIST -> if (layout.containerType == ContainerType.COLUMN) 0.2f else 0f
            ContentType.FORM -> if (layout.containerType == ContainerType.COLUMN) 0.2f else 0f
            ContentType.CHART -> if (layout.containerType == ContainerType.BOX) 0.2f else 0f
            else -> 0.1f
        }

        // 检查组件优先级排序
        val sortedByPriority = layout.components.sortedByDescending { it.priority }
        if (sortedByPriority == layout.components) score += 0.1f

        // 检查屏幕尺寸适配
        score += when (context.screenSize) {
            ScreenSize.COMPACT -> if (layout.containerType == ContainerType.COLUMN) 0.1f else 0f
            ScreenSize.EXPANDED -> if (layout.containerType in listOf(ContainerType.ROW, ContainerType.GRID)) 0.1f else 0f
            else -> 0.05f
        }

        return score.coerceIn(0f, 1f)
    }

    private fun calculateOverallScore(
        rendering: Float,
        memory: Float,
        responsive: Float,
        accessibility: Float,
        ux: Float
    ): Float {
        return (rendering * 0.25f + memory * 0.15f + responsive * 0.25f + accessibility * 0.15f + ux * 0.2f)
            .coerceIn(0f, 1f)
    }

    private fun calculateNestingDepth(layout: LayoutConfiguration): Int {
        // 简化实现，实际应该递归计算
        return layout.components.size / 10
    }
}

/**
 * 布局性能评分
 */
data class LayoutPerformanceScore(
    val rendering: Float,
    val memory: Float,
    val responsive: Float,
    val accessibility: Float,
    val userExperience: Float,
    val overall: Float
) {
    fun getGrade(): LayoutGrade {
        return when {
            overall >= 0.9f -> LayoutGrade.EXCELLENT
            overall >= 0.8f -> LayoutGrade.GOOD
            overall >= 0.7f -> LayoutGrade.FAIR
            overall >= 0.6f -> LayoutGrade.POOR
            else -> LayoutGrade.CRITICAL
        }
    }

    fun getRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()

        if (rendering < 0.7f) recommendations.add("优化渲染性能：减少组件数量或简化布局结构")
        if (memory < 0.7f) recommendations.add("优化内存使用：减少重型组件或实现懒加载")
        if (responsive < 0.7f) recommendations.add("改善响应式设计：添加断点配置和弹性布局")
        if (accessibility < 0.7f) recommendations.add("提升可访问性：增加间距和改善对比度")
        if (userExperience < 0.7f) recommendations.add("优化用户体验：调整布局类型和组件排序")

        return recommendations
    }
}

enum class LayoutGrade {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

/**
 * 智能布局建议器
 */
class SmartLayoutSuggester {
    private val performanceEvaluator = LayoutPerformanceEvaluator()
    private val responsiveAdapter = ResponsiveLayoutAdapter()

    /**
     * 生成智能布局建议
     */
    fun generateSuggestions(
        layout: LayoutConfiguration,
        context: LayoutContext
    ): List<LayoutSuggestion> {
        val currentScore = performanceEvaluator.evaluateLayout(layout, context)
        val suggestions = mutableListOf<LayoutSuggestion>()

        // 1. 响应式优化建议
        if (currentScore.responsive < 0.8f) {
            val responsiveLayout = responsiveAdapter.adaptLayoutForScreen(
                layout, context.screenSize, context.deviceInfo
            )
            val responsiveScore = performanceEvaluator.evaluateLayout(responsiveLayout, context)

            if (responsiveScore.overall > currentScore.overall) {
                suggestions.add(
                    LayoutSuggestion(
                        type = SuggestionType.RESPONSIVE_OPTIMIZATION,
                        title = "响应式布局优化",
                        description = "根据当前屏幕尺寸优化布局结构",
                        layout = responsiveLayout,
                        expectedImprovement = responsiveScore.overall - currentScore.overall,
                        priority = SuggestionPriority.HIGH
                    )
                )
            }
        }

        // 2. 性能优化建议
        if (currentScore.rendering < 0.7f) {
            suggestions.add(generatePerformanceSuggestion(layout, context, currentScore))
        }

        // 3. 可访问性建议
        if (currentScore.accessibility < 0.8f) {
            suggestions.add(generateAccessibilitySuggestion(layout, context, currentScore))
        }

        // 4. 用户体验建议
        if (currentScore.userExperience < 0.8f) {
            suggestions.add(generateUXSuggestion(layout, context, currentScore))
        }

        return suggestions.sortedByDescending { it.priority.ordinal }
    }

    private fun generatePerformanceSuggestion(
        layout: LayoutConfiguration,
        context: LayoutContext,
        currentScore: LayoutPerformanceScore
    ): LayoutSuggestion {
        val optimizedLayout = layout.copy(
            containerType = when (layout.containerType) {
                ContainerType.ADAPTIVE -> ContainerType.GRID
                ContainerType.FLOW -> ContainerType.COLUMN
                else -> layout.containerType
            },
            components = layout.components.take(20) // 限制组件数量
        )

        return LayoutSuggestion(
            type = SuggestionType.PERFORMANCE_OPTIMIZATION,
            title = "性能优化",
            description = "简化布局结构以提升渲染性能",
            layout = optimizedLayout,
            expectedImprovement = 0.2f,
            priority = SuggestionPriority.HIGH
        )
    }

    private fun generateAccessibilitySuggestion(
        layout: LayoutConfiguration,
        context: LayoutContext,
        currentScore: LayoutPerformanceScore
    ): LayoutSuggestion {
        val optimizedLayout = layout.copy(
            spacing = maxOf(layout.spacing, 12.dp),
            padding = PaddingValues(
                horizontal = maxOf(
                    layout.padding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    16.dp
                ),
                vertical = maxOf(layout.padding.calculateTopPadding(), 12.dp)
            )
        )

        return LayoutSuggestion(
            type = SuggestionType.ACCESSIBILITY_IMPROVEMENT,
            title = "可访问性改进",
            description = "增加间距和内边距以提升可访问性",
            layout = optimizedLayout,
            expectedImprovement = 0.15f,
            priority = SuggestionPriority.MEDIUM
        )
    }

    private fun generateUXSuggestion(
        layout: LayoutConfiguration,
        context: LayoutContext,
        currentScore: LayoutPerformanceScore
    ): LayoutSuggestion {
        val optimizedContainerType = when (context.contentType) {
            ContentType.DASHBOARD -> ContainerType.GRID
            ContentType.LIST -> ContainerType.COLUMN
            ContentType.FORM -> ContainerType.COLUMN
            ContentType.CHART -> ContainerType.BOX
            else -> layout.containerType
        }

        val optimizedLayout = layout.copy(
            containerType = optimizedContainerType,
            arrangement = LayoutArrangement.TOP_TO_BOTTOM // 重要内容优先
        )

        return LayoutSuggestion(
            type = SuggestionType.UX_ENHANCEMENT,
            title = "用户体验优化",
            description = "调整布局类型以更好地适配内容",
            layout = optimizedLayout,
            expectedImprovement = 0.1f,
            priority = SuggestionPriority.MEDIUM
        )
    }
}

/**
 * 布局建议
 */
data class LayoutSuggestion(
    val type: SuggestionType,
    val title: String,
    val description: String,
    val layout: LayoutConfiguration,
    val expectedImprovement: Float,
    val priority: SuggestionPriority
)

enum class SuggestionType {
    PERFORMANCE_OPTIMIZATION,
    RESPONSIVE_OPTIMIZATION,
    ACCESSIBILITY_IMPROVEMENT,
    UX_ENHANCEMENT,
    CONTENT_OPTIMIZATION
}

enum class SuggestionPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}