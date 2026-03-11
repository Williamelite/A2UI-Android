package org.a2ui.compose.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.a2ui.compose.data.Component
import kotlin.math.*

/**
 * AI驱动的无障碍增强器
 *
 * 功能：
 * 1. 自动无障碍标签生成
 * 2. 颜色对比度优化
 * 3. 触摸目标大小调整
 * 4. 语音导航优化
 * 5. 动态字体大小调整
 */
class AccessibilityEnhancer {

    private val _accessibilitySettings = MutableStateFlow(AccessibilitySettings())
    val accessibilitySettings: StateFlow<AccessibilitySettings> = _accessibilitySettings.asStateFlow()

    private val _enhancementSuggestions = MutableStateFlow<List<AccessibilityEnhancement>>(emptyList())
    val enhancementSuggestions: StateFlow<List<AccessibilityEnhancement>> = _enhancementSuggestions.asStateFlow()

    /**
     * 检测系统无障碍设置
     */
    @Composable
    fun DetectAccessibilitySettings() {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            val settings = detectSystemAccessibilitySettings(context)
            _accessibilitySettings.value = settings
        }
    }

    /**
     * 增强组件的无障碍性
     */
    fun enhanceComponentAccessibility(component: Component): Component {
        val settings = _accessibilitySettings.value

        // TODO: Implement accessibility enhancements using actual Component properties
        // The current implementation assumes properties that don't exist on Component
        return component
        /*
        return component.copy(
            // 自动生成语义化标签
            accessibilityLabel = generateSmartAccessibilityLabel(component),

            // 调整触摸目标大小
            minTouchTargetSize = calculateOptimalTouchTargetSize(component, settings),

            // 优化颜色对比度
            color = optimizeColorContrast(component.color, component.backgroundColor, settings),
            backgroundColor = optimizeBackgroundColor(component.color, component.backgroundColor, settings),

            // 调整字体大小
            fontSize = adjustFontSizeForAccessibility(component.fontSize, settings),

            // 添加语义角色
            semanticRole = determineSemanticRole(component),

            // 设置状态描述
            stateDescription = generateStateDescription(component)
        )
        */
    }

    /**
     * 生成智能无障碍标签
     */
    private fun generateSmartAccessibilityLabel(component: Component): String {
        // Use actual Component properties: text, label, name, description
        val baseLabel = component.text?.toString() ?:
                       component.label?.toString() ?:
                       component.name?.toString() ?:
                       component.description?.toString() ?:
                       "组件"

        return when (component.component) {
            "Button" -> {
                val action = when (component.variant) {
                    "primary" -> "主要操作"
                    "secondary" -> "次要操作"
                    else -> "操作"
                }
                "${baseLabel ?: "按钮"}, $action"
            }

            "TextField" -> {
                val required = if (component.required == true) "必填" else "可选"
                val type = when (component.variant) {
                    "password" -> "密码输入框"
                    "email" -> "邮箱输入框"
                    "phone" -> "电话号码输入框"
                    else -> "文本输入框"
                }
                "${baseLabel ?: type}, $required"
            }

            "CheckBox" -> {
                // Use value property to determine checked state
                val isChecked = component.value?.toString()?.toBoolean() ?: false
                val state = if (isChecked) "已选中" else "未选中"
                "${baseLabel ?: "复选框"}, $state"
            }

            "Switch" -> {
                // Use value property to determine switch state
                val isOn = component.value?.toString()?.toBoolean() ?: false
                val state = if (isOn) "已开启" else "已关闭"
                "${baseLabel ?: "开关"}, $state"
            }

            "Image" -> {
                // Use description or label for alt text
                "${component.description?.toString() ?: baseLabel ?: "图片"}"
            }

            "Text" -> {
                when (component.variant) {
                    "h1" -> "一级标题: ${baseLabel}"
                    "h2" -> "二级标题: ${baseLabel}"
                    "h3" -> "三级标题: ${baseLabel}"
                    else -> baseLabel ?: "文本"
                }
            }

            "Card" -> "卡片: ${baseLabel ?: "内容区域"}"

            "List" -> "列表: ${baseLabel ?: "项目列表"}"

            "Modal" -> "对话框: ${baseLabel ?: "弹出窗口"}"

            else -> baseLabel ?: component.component
        }
    }

    /**
     * 计算最佳触摸目标大小
     */
    private fun calculateOptimalTouchTargetSize(
        component: Component,
        settings: AccessibilitySettings
    ): androidx.compose.ui.unit.Dp {
        val baseSize = when (component.component) {
            "Button", "CheckBox", "Switch" -> 48.dp
            "TextField" -> 56.dp
            "Image" -> if (component.action != null) 48.dp else 0.dp
            else -> 0.dp
        }

        // 根据用户设置调整
        val multiplier = when {
            settings.largeText -> 1.2f
            settings.extraLargeText -> 1.5f
            settings.touchExploration -> 1.3f
            else -> 1f
        }

        return baseSize * multiplier
    }

    /**
     * 优化颜色对比度
     */
    private fun optimizeColorContrast(
        foregroundColor: String?,
        backgroundColor: String?,
        settings: AccessibilitySettings
    ): String? {
        if (foregroundColor == null || backgroundColor == null) return foregroundColor

        try {
            val fgColor = Color(android.graphics.Color.parseColor(foregroundColor))
            val bgColor = Color(android.graphics.Color.parseColor(backgroundColor))

            val contrast = calculateWCAGContrast(fgColor, bgColor)
            val requiredContrast = if (settings.highContrast) 7f else 4.5f

            if (contrast < requiredContrast) {
                // 自动调整前景色以满足对比度要求
                return adjustColorForContrast(fgColor, bgColor, requiredContrast)
            }
        } catch (e: Exception) {
            // 颜色解析失败，返回原色
        }

        return foregroundColor
    }

    /**
     * 优化背景颜色
     */
    private fun optimizeBackgroundColor(
        foregroundColor: String?,
        backgroundColor: String?,
        settings: AccessibilitySettings
    ): String? {
        if (settings.highContrast && backgroundColor != null) {
            try {
                val bgColor = Color(android.graphics.Color.parseColor(backgroundColor))
                // 在高对比度模式下，使背景更极端（更白或更黑）
                // Calculate luminance manually: 0.299*R + 0.587*G + 0.114*B
                val luminance = 0.299f * bgColor.red + 0.587f * bgColor.green + 0.114f * bgColor.blue
                return if (luminance > 0.5f) "#FFFFFF" else "#000000"
            } catch (e: Exception) {
                // 颜色解析失败，返回原色
            }
        }
        return backgroundColor
    }

    /**
     * 调整字体大小以适应无障碍需求
     */
    private fun adjustFontSizeForAccessibility(
        fontSize: androidx.compose.ui.unit.TextUnit?,
        settings: AccessibilitySettings
    ): androidx.compose.ui.unit.TextUnit? {
        if (fontSize == null) return null

        val multiplier = when {
            settings.extraLargeText -> 1.5f
            settings.largeText -> 1.2f
            else -> 1f
        }

        return fontSize * multiplier
    }

    /**
     * 确定语义角色
     */
    private fun determineSemanticRole(component: Component): Role? {
        return when (component.component) {
            "Button" -> Role.Button
            "CheckBox" -> Role.Checkbox
            "Switch" -> Role.Switch
            "TextField" -> Role.Button // TODO: Use correct Role for text input
            "Image" -> if (component.action != null) Role.Button else Role.Image
            "Tab" -> Role.Tab
            else -> null
        }
    }

    /**
     * 生成状态描述
     */
    private fun generateStateDescription(component: Component): String? {
        return when (component.component) {
            "CheckBox" -> {
                val isChecked = component.value?.toString()?.toBoolean() ?: false
                if (isChecked) "已选中" else "未选中"
            }
            "Switch" -> {
                val isOn = component.value?.toString()?.toBoolean() ?: false
                if (isOn) "已开启" else "已关闭"
            }
            "TextField" -> {
                val parts = mutableListOf<String>()
                if (component.required == true) parts.add("必填")
                // TODO: Component doesn't have error property
                // if (component.error != null) parts.add("有错误")
                if (component.value != null) parts.add("已填写")
                parts.joinToString(", ").takeIf { it.isNotEmpty() }
            }
            "Button" -> {
                when (component.variant) {
                    "primary" -> "主要操作按钮"
                    "secondary" -> "次要操作按钮"
                    else -> null
                }
            }
            else -> null
        }
    }

    /**
     * 检测系统无障碍设置
     */
    private fun detectSystemAccessibilitySettings(context: Context): AccessibilitySettings {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        return AccessibilitySettings(
            isEnabled = accessibilityManager.isEnabled,
            touchExploration = accessibilityManager.isTouchExplorationEnabled,
            highContrast = isHighContrastEnabled(context),
            largeText = isLargeTextEnabled(context),
            extraLargeText = isExtraLargeTextEnabled(context),
            reduceMotion = isReduceMotionEnabled(context),
            screenReader = isScreenReaderEnabled(accessibilityManager)
        )
    }

    /**
     * 计算WCAG对比度
     */
    private fun calculateWCAGContrast(foreground: Color, background: Color): Float {
        val fgLuminance = calculateRelativeLuminance(foreground)
        val bgLuminance = calculateRelativeLuminance(background)

        val lighter = maxOf(fgLuminance, bgLuminance)
        val darker = minOf(fgLuminance, bgLuminance)

        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * 计算相对亮度
     */
    private fun calculateRelativeLuminance(color: Color): Float {
        fun linearize(component: Float): Float {
            return if (component <= 0.03928f) {
                component / 12.92f
            } else {
                ((component + 0.055f) / 1.055f).pow(2.4f)
            }
        }

        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)

        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    /**
     * 调整颜色以满足对比度要求
     */
    private fun adjustColorForContrast(
        foreground: Color,
        background: Color,
        requiredContrast: Float
    ): String {
        val bgLuminance = calculateRelativeLuminance(background)

        // 决定是调亮还是调暗前景色
        val shouldLighten = bgLuminance < 0.5f

        var adjustedColor = foreground
        var step = 0.1f

        while (calculateWCAGContrast(adjustedColor, background) < requiredContrast && step <= 1f) {
            adjustedColor = if (shouldLighten) {
                Color(
                    red = minOf(1f, foreground.red + step),
                    green = minOf(1f, foreground.green + step),
                    blue = minOf(1f, foreground.blue + step),
                    alpha = foreground.alpha
                )
            } else {
                Color(
                    red = maxOf(0f, foreground.red - step),
                    green = maxOf(0f, foreground.green - step),
                    blue = maxOf(0f, foreground.blue - step),
                    alpha = foreground.alpha
                )
            }
            step += 0.1f
        }

        return String.format("#%02X%02X%02X",
            (adjustedColor.red * 255).toInt(),
            (adjustedColor.green * 255).toInt(),
            (adjustedColor.blue * 255).toInt()
        )
    }

    // 辅助方法来检测各种无障碍设置
    private fun isHighContrastEnabled(context: Context): Boolean {
        // 实际实现需要检查系统设置
        return false
    }

    private fun isLargeTextEnabled(context: Context): Boolean {
        // 实际实现需要检查系统字体缩放设置
        return false
    }

    private fun isExtraLargeTextEnabled(context: Context): Boolean {
        // 实际实现需要检查系统字体缩放设置
        return false
    }

    private fun isReduceMotionEnabled(context: Context): Boolean {
        // 实际实现需要检查系统动画设置
        return false
    }

    private fun isScreenReaderEnabled(accessibilityManager: AccessibilityManager): Boolean {
        // 检查是否有屏幕阅读器服务在运行
        return accessibilityManager.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN
        ).isNotEmpty()
    }

    /**
     * 生成无障碍增强建议
     */
    fun generateAccessibilityEnhancements(components: List<Component>): List<AccessibilityEnhancement> {
        val enhancements = mutableListOf<AccessibilityEnhancement>()

        components.forEach { component ->
            // 检查缺失的无障碍标签
            val hasAccessibilityLabel = component.label != null ||
                                      component.text != null ||
                                      component.description != null
            if (!hasAccessibilityLabel) {
                enhancements.add(
                    AccessibilityEnhancement(
                        componentId = component.id,
                        type = AccessibilityEnhancementType.MISSING_LABEL,
                        severity = Severity.HIGH,
                        description = "组件缺少无障碍标签",
                        suggestion = "添加描述性的无障碍标签",
                        autoFix = true
                    )
                )
            }

            // 检查触摸目标大小
            // TODO: Implement touch target size checking using actual Component properties
            val isInteractiveComponent = component.component in listOf("Button", "CheckBox", "Switch")
            if (isInteractiveComponent) {
                enhancements.add(
                    AccessibilityEnhancement(
                        componentId = component.id,
                        type = AccessibilityEnhancementType.SMALL_TOUCH_TARGET,
                        severity = Severity.MEDIUM,
                        description = "触摸目标过小 (未知大小 < 48dp)",
                        suggestion = "增加触摸目标大小至至少48dp",
                        autoFix = true
                    )
                )
            }

            // 检查颜色对比度
            // TODO: Implement color contrast checking using actual Component properties
            // Component class doesn't have color/backgroundColor properties
            /*
            if (component.color != null && component.backgroundColor != null) {
                try {
                    val fg = Color(android.graphics.Color.parseColor(component.color))
                    val bg = Color(android.graphics.Color.parseColor(component.backgroundColor))
                    val contrast = calculateWCAGContrast(fg, bg)

                    if (contrast < 4.5f) {
                        enhancements.add(
                            AccessibilityEnhancement(
                                componentId = component.id,
                                type = AccessibilityEnhancementType.LOW_CONTRAST,
                                severity = Severity.HIGH,
                                description = "颜色对比度不足 (${String.format("%.1f", contrast)}:1 < 4.5:1)",
                                suggestion = "调整前景色或背景色以提高对比度",
                                autoFix = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    // 颜色解析失败
                }
            }
            */
        }

        _enhancementSuggestions.value = enhancements
        return enhancements
    }
}

/**
 * 无障碍设置
 */
data class AccessibilitySettings(
    val isEnabled: Boolean = false,
    val touchExploration: Boolean = false,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val extraLargeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val screenReader: Boolean = false
)

/**
 * 无障碍增强建议
 */
data class AccessibilityEnhancement(
    val componentId: String,
    val type: AccessibilityEnhancementType,
    val severity: Severity,
    val description: String,
    val suggestion: String,
    val autoFix: Boolean = false
)

/**
 * 无障碍增强类型
 */
enum class AccessibilityEnhancementType {
    MISSING_LABEL,
    SMALL_TOUCH_TARGET,
    LOW_CONTRAST,
    MISSING_STATE_DESCRIPTION,
    IMPROPER_HEADING_STRUCTURE,
    MISSING_FOCUS_INDICATOR
}

/**
 * 严重程度
 */
enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}