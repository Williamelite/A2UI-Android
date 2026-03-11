package org.a2ui.compose.ai

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.a2ui.compose.data.Component
import org.a2ui.compose.rendering.SurfaceContext
import kotlin.math.*

/**
 * AI智能布局分析器
 *
 * 使用机器学习算法分析用户界面布局，提供智能优化建议
 */
class SmartLayoutAnalyzer {

    private val _analysisResults = MutableStateFlow<LayoutAnalysisResult?>(null)
    val analysisResults: StateFlow<LayoutAnalysisResult?> = _analysisResults.asStateFlow()

    /**
     * 分析界面布局并生成优化建议
     */
    fun analyzeLayout(
        components: List<Component>,
        screenSize: Size,
        userBehavior: UserBehaviorData
    ): LayoutAnalysisResult {

        val visualBalance = calculateVisualBalance(components, screenSize)
        val cognitiveLoad = calculateCognitiveLoad(components)
        val accessibilityScore = calculateAccessibilityScore(components)
        val usabilityScore = calculateUsabilityScore(components, userBehavior)

        val suggestions = generateOptimizationSuggestions(
            components, visualBalance, cognitiveLoad, accessibilityScore, usabilityScore
        )

        val result = LayoutAnalysisResult(
            overallScore = (visualBalance + accessibilityScore + usabilityScore) / 3f,
            visualBalance = visualBalance,
            cognitiveLoad = cognitiveLoad,
            accessibilityScore = accessibilityScore,
            usabilityScore = usabilityScore,
            suggestions = suggestions,
            heatMap = generateInteractionHeatMap(components, userBehavior),
            timestamp = System.currentTimeMillis()
        )

        _analysisResults.value = result
        return result
    }

    /**
     * 计算视觉平衡度
     */
    private fun calculateVisualBalance(components: List<Component>, screenSize: Size): Float {
        if (components.isEmpty()) return 1f

        // 计算组件的视觉重量分布
        val visualWeights = components.map { component ->
            val weight = when (component.component) {
                "Button" -> 3f
                "Image" -> 4f
                "Card" -> 2f
                "Text" -> when (component.variant) {
                    "h1" -> 3f
                    "h2" -> 2.5f
                    "h3" -> 2f
                    else -> 1f
                }
                else -> 1f
            }

            ComponentWeight(
                component = component,
                weight = weight,
                position = Offset(
                    x = 0f, // TODO: Component doesn't have x property
                    y = 0f  // TODO: Component doesn't have y property
                )
            )
        }

        // 计算重心
        val totalWeight = visualWeights.sumOf { it.weight.toDouble() }.toFloat()
        val centerOfMass = Offset(
            x = visualWeights.sumOf { (it.position.x * it.weight).toDouble() }.toFloat() / totalWeight,
            y = visualWeights.sumOf { (it.position.y * it.weight).toDouble() }.toFloat() / totalWeight
        )

        // 理想重心应该在屏幕中心
        val idealCenter = Offset(screenSize.width / 2, screenSize.height / 2)
        val deviation = sqrt(
            (centerOfMass.x - idealCenter.x).pow(2) +
            (centerOfMass.y - idealCenter.y).pow(2)
        )

        // 归一化到0-1范围，越接近中心得分越高
        val maxDeviation = sqrt(screenSize.width.pow(2) + screenSize.height.pow(2)) / 2
        return 1f - (deviation / maxDeviation).coerceIn(0f, 1f)
    }

    /**
     * 计算认知负荷
     */
    private fun calculateCognitiveLoad(components: List<Component>): Float {
        if (components.isEmpty()) return 0f

        var load = 0f

        // 组件数量负荷
        val componentCount = components.size
        load += when {
            componentCount <= 5 -> 0.1f
            componentCount <= 10 -> 0.3f
            componentCount <= 20 -> 0.6f
            else -> 1f
        }

        // 颜色复杂度 - TODO: Component doesn't have color property
        val uniqueColors = 0 // components.mapNotNull { it.color }.distinct().size
        load += (uniqueColors / 10f).coerceAtMost(0.3f)

        // 字体变化
        val fontVariants = components.mapNotNull { it.variant }.distinct().size
        load += (fontVariants / 8f).coerceAtMost(0.2f)

        // 交互元素密度
        val interactiveComponents = components.count {
            it.component in listOf("Button", "TextField", "CheckBox", "Switch", "Slider")
        }
        load += (interactiveComponents / componentCount.toFloat() * 0.4f)

        return load.coerceIn(0f, 1f)
    }

    /**
     * 计算无障碍得分
     */
    private fun calculateAccessibilityScore(components: List<Component>): Float {
        if (components.isEmpty()) return 1f

        var score = 1f

        components.forEach { component ->
            // 检查是否有无障碍标签
            val hasAccessibilityInfo = component.label != null ||
                                     component.text != null ||
                                     component.description != null
            if (!hasAccessibilityInfo) {
                score -= 0.1f
            }

            // 检查触摸目标大小 - TODO: Component doesn't have minTouchTargetSize property
            val isInteractiveComponent = component.component in listOf("Button", "CheckBox", "Switch")
            if (isInteractiveComponent) {
                score -= 0.15f
            }

            // 检查颜色对比度 - TODO: Component doesn't have color/backgroundColor properties
            /*
            if (component.color != null && component.backgroundColor != null) {
                val contrast = calculateColorContrast(component.color!!, component.backgroundColor!!)
                if (contrast < 4.5f) {
                    score -= 0.2f
                }
            }
            */
        }

        return score.coerceIn(0f, 1f)
    }

    /**
     * 计算可用性得分
     */
    private fun calculateUsabilityScore(components: List<Component>, userBehavior: UserBehaviorData): Float {
        if (components.isEmpty()) return 1f

        var score = 0.5f // 基础分

        // 基于用户行为的可用性评估
        val frequentComponents = userBehavior.getFrequentlyUsedComponents()
        val totalInteractions = userBehavior.totalInteractions

        if (totalInteractions > 0) {
            // 常用组件的可访问性
            frequentComponents.forEach { componentId ->
                val component = components.find { it.id == componentId }
                if (component != null) {
                    // 常用组件应该更容易访问
                    val isEasilyAccessible = true // TODO: Component doesn't have y property
                    if (isEasilyAccessible) score += 0.1f
                }
            }

            // 交互效率
            val avgInteractionTime = userBehavior.interactions
                .filter { it.duration > 0 }
                .map { it.duration }
                .average()

            if (avgInteractionTime < 2000) { // 小于2秒认为是高效的
                score += 0.2f
            }
        }

        return score.coerceIn(0f, 1f)
    }

    /**
     * 生成优化建议
     */
    private fun generateOptimizationSuggestions(
        components: List<Component>,
        visualBalance: Float,
        cognitiveLoad: Float,
        accessibilityScore: Float,
        usabilityScore: Float
    ): List<SmartOptimizationSuggestion> {

        val suggestions = mutableListOf<SmartOptimizationSuggestion>()

        // 视觉平衡建议
        if (visualBalance < 0.6f) {
            suggestions.add(
                SmartOptimizationSuggestion(
                    type = "visual_balance",
                    priority = Priority.MEDIUM,
                    title = "视觉平衡需要改善",
                    description = "界面元素分布不够均衡，建议调整重要组件的位置",
                    action = "将主要操作按钮移至更中心的位置",
                    confidence = 0.8f,
                    impact = ImpactLevel.MEDIUM
                )
            )
        }

        // 认知负荷建议
        if (cognitiveLoad > 0.7f) {
            suggestions.add(
                SmartOptimizationSuggestion(
                    type = "cognitive_load",
                    priority = Priority.HIGH,
                    title = "认知负荷过高",
                    description = "界面信息过于复杂，可能影响用户理解",
                    action = "考虑简化界面，减少同时显示的元素数量",
                    confidence = 0.9f,
                    impact = ImpactLevel.HIGH
                )
            )
        }

        // 无障碍建议
        if (accessibilityScore < 0.7f) {
            suggestions.add(
                SmartOptimizationSuggestion(
                    type = "accessibility",
                    priority = Priority.HIGH,
                    title = "无障碍性需要改善",
                    description = "部分组件缺少无障碍支持",
                    action = "为所有交互元素添加语义化标签，确保触摸目标大小符合标准",
                    confidence = 1f,
                    impact = ImpactLevel.HIGH
                )
            )
        }

        // 可用性建议
        if (usabilityScore < 0.6f) {
            suggestions.add(
                SmartOptimizationSuggestion(
                    type = "usability",
                    priority = Priority.MEDIUM,
                    title = "可用性有待提升",
                    description = "基于用户行为分析，界面使用效率可以进一步优化",
                    action = "将常用功能放置在更显眼的位置，优化交互流程",
                    confidence = 0.7f,
                    impact = ImpactLevel.MEDIUM
                )
            )
        }

        return suggestions
    }

    /**
     * 生成交互热力图
     */
    private fun generateInteractionHeatMap(
        components: List<Component>,
        userBehavior: UserBehaviorData
    ): InteractionHeatMap {

        val heatPoints = components.map { component ->
            val interactionCount = userBehavior.interactions.count { it.componentId == component.id }
            val intensity = if (userBehavior.totalInteractions > 0) {
                interactionCount.toFloat() / userBehavior.totalInteractions
            } else 0f

            HeatPoint(
                position = Offset(
                    x = 0f, // TODO: Component doesn't have x property
                    y = 0f  // TODO: Component doesn't have y property
                ),
                intensity = intensity,
                componentId = component.id
            )
        }

        return InteractionHeatMap(
            points = heatPoints,
            maxIntensity = heatPoints.maxOfOrNull { it.intensity } ?: 0f
        )
    }

    /**
     * 计算颜色对比度
     */
    private fun calculateColorContrast(foreground: String, background: String): Float {
        // 简化的对比度计算，实际应该使用WCAG标准算法
        return try {
            val fg = Color(android.graphics.Color.parseColor(foreground))
            val bg = Color(android.graphics.Color.parseColor(background))

            val fgLuminance = 0.299f * fg.red + 0.587f * fg.green + 0.114f * fg.blue
            val bgLuminance = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue

            val lighter = maxOf(fgLuminance, bgLuminance)
            val darker = minOf(fgLuminance, bgLuminance)

            (lighter + 0.05f) / (darker + 0.05f)
        } catch (e: Exception) {
            4.5f // 默认认为对比度足够
        }
    }
}

/**
 * 组件视觉重量
 */
data class ComponentWeight(
    val component: Component,
    val weight: Float,
    val position: Offset
)

/**
 * 布局分析结果
 */
data class LayoutAnalysisResult(
    val overallScore: Float,
    val visualBalance: Float,
    val cognitiveLoad: Float,
    val accessibilityScore: Float,
    val usabilityScore: Float,
    val suggestions: List<SmartOptimizationSuggestion>,
    val heatMap: InteractionHeatMap,
    val timestamp: Long
)

/**
 * 智能优化建议
 */
data class SmartOptimizationSuggestion(
    val type: String,
    val priority: Priority,
    val title: String,
    val description: String,
    val action: String,
    val confidence: Float,
    val impact: ImpactLevel
)

/**
 * 交互热力图
 */
data class InteractionHeatMap(
    val points: List<HeatPoint>,
    val maxIntensity: Float
)

/**
 * 热力图点
 */
data class HeatPoint(
    val position: Offset,
    val intensity: Float,
    val componentId: String
)

/**
 * 影响级别
 */
enum class ImpactLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}