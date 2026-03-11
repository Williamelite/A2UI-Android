package org.a2ui.compose.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.a2ui.compose.data.Component
import org.a2ui.compose.rendering.A2UIRenderer
import org.a2ui.compose.rendering.SurfaceContext

/**
 * AI驱动的响应式布局管理器
 *
 * 功能：
 * 1. 智能断点管理
 * 2. 自适应组件排列
 * 3. 动态间距调整
 * 4. 设备特性感知
 */
class ResponsiveLayoutManager(
    private val renderer: A2UIRenderer,
    private val layoutOptimizer: LayoutOptimizer
) {
    private val _currentBreakpoint = MutableStateFlow(Breakpoint.COMPACT)
    val currentBreakpoint: StateFlow<Breakpoint> = _currentBreakpoint.asStateFlow()

    private val _layoutConfiguration = MutableStateFlow(ResponsiveLayoutConfiguration())
    val layoutConfiguration: StateFlow<ResponsiveLayoutConfiguration> = _layoutConfiguration.asStateFlow()

    /**
     * 智能响应式布局组合函数
     */
    @Composable
    fun SmartResponsiveLayout(
        surfaceId: String,
        modifier: Modifier = Modifier,
        content: @Composable BoxScope.() -> Unit
    ) {
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current

        val screenSize = with(density) {
            Size(
                width = configuration.screenWidthDp.dp.toPx(),
                height = configuration.screenHeightDp.dp.toPx()
            )
        }

        // 动态计算最佳断点
        val breakpoint = calculateOptimalBreakpoint(screenSize, configuration)
        LaunchedEffect(breakpoint) {
            _currentBreakpoint.value = breakpoint
        }

        // 获取智能布局配置
        val smartConfig = generateSmartLayoutConfiguration(breakpoint, screenSize)
        LaunchedEffect(smartConfig) {
            _layoutConfiguration.value = smartConfig
        }

        // 应用响应式布局
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(smartConfig.containerPadding)
        ) {
            content()
        }
    }

    /**
     * 智能网格布局
     */
    @Composable
    fun SmartGrid(
        components: List<Component>,
        modifier: Modifier = Modifier
    ) {
        val config = layoutConfiguration.collectAsState().value
        val breakpoint = currentBreakpoint.collectAsState().value

        val columns = when (breakpoint) {
            Breakpoint.COMPACT -> config.compactColumns
            Breakpoint.MEDIUM -> config.mediumColumns
            Breakpoint.EXPANDED -> config.expandedColumns
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(config.gridSpacing),
            horizontalArrangement = Arrangement.spacedBy(config.gridSpacing),
            verticalArrangement = Arrangement.spacedBy(config.gridSpacing),
            modifier = modifier
        ) {
            items(components.size) { index ->
                val component = components[index]
                SmartGridItem(
                    component = component,
                    breakpoint = breakpoint,
                    config = config
                )
            }
        }
    }

    /**
     * 智能网格项
     */
    @Composable
    private fun SmartGridItem(
        component: Component,
        breakpoint: Breakpoint,
        config: ResponsiveLayoutConfiguration
    ) {
        val itemModifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                // 根据组件类型和断点调整高度
                when (component.component) {
                    "Card" -> modifier.aspectRatio(
                        when (breakpoint) {
                            Breakpoint.COMPACT -> 1f
                            Breakpoint.MEDIUM -> 1.2f
                            Breakpoint.EXPANDED -> 1.5f
                        }
                    )
                    "Button" -> modifier.height(config.buttonHeight)
                    "TextField" -> modifier.height(config.textFieldHeight)
                    else -> modifier.wrapContentHeight()
                }
            }

        // TODO: Fix component rendering - Component doesn't have surfaceId property
        // renderer.registry.render(component, renderer.getSurfaceContext(""))
    }

    /**
     * 自适应间距
     */
    @Composable
    fun AdaptiveSpacing(
        vertical: Boolean = true,
        factor: Float = 1f
    ): Dp {
        val config = layoutConfiguration.collectAsState().value
        val baseSpacing = if (vertical) config.verticalSpacing else config.horizontalSpacing
        return baseSpacing * factor
    }

    /**
     * 计算最佳断点
     */
    @Composable
    private fun calculateOptimalBreakpoint(
        screenSize: Size,
        configuration: android.content.res.Configuration
    ): Breakpoint {
        val widthDp = screenSize.width / LocalDensity.current.density

        // 考虑设备方向
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        return when {
            widthDp < 600 -> Breakpoint.COMPACT
            widthDp < 840 -> if (isLandscape) Breakpoint.EXPANDED else Breakpoint.MEDIUM
            else -> Breakpoint.EXPANDED
        }
    }

    /**
     * 生成智能布局配置
     */
    private fun generateSmartLayoutConfiguration(
        breakpoint: Breakpoint,
        screenSize: Size
    ): ResponsiveLayoutConfiguration {
        val density = screenSize.width / 360f // 以360dp为基准

        return when (breakpoint) {
            Breakpoint.COMPACT -> ResponsiveLayoutConfiguration(
                compactColumns = 1,
                mediumColumns = 2,
                expandedColumns = 3,
                containerPadding = (16 * density).dp,
                gridSpacing = (12 * density).dp,
                verticalSpacing = (16 * density).dp,
                horizontalSpacing = (12 * density).dp,
                buttonHeight = (48 * density).dp,
                textFieldHeight = (56 * density).dp,
                cardElevation = (4 * density).dp
            )
            Breakpoint.MEDIUM -> ResponsiveLayoutConfiguration(
                compactColumns = 2,
                mediumColumns = 3,
                expandedColumns = 4,
                containerPadding = (24 * density).dp,
                gridSpacing = (16 * density).dp,
                verticalSpacing = (20 * density).dp,
                horizontalSpacing = (16 * density).dp,
                buttonHeight = (52 * density).dp,
                textFieldHeight = (60 * density).dp,
                cardElevation = (6 * density).dp
            )
            Breakpoint.EXPANDED -> ResponsiveLayoutConfiguration(
                compactColumns = 3,
                mediumColumns = 4,
                expandedColumns = 6,
                containerPadding = (32 * density).dp,
                gridSpacing = (20 * density).dp,
                verticalSpacing = (24 * density).dp,
                horizontalSpacing = (20 * density).dp,
                buttonHeight = (56 * density).dp,
                textFieldHeight = (64 * density).dp,
                cardElevation = (8 * density).dp
            )
        }
    }

    /**
     * 获取组件的智能尺寸
     */
    fun getSmartComponentSize(
        component: Component,
        breakpoint: Breakpoint
    ): ComponentSize {
        val baseWidth = when (component.component) {
            "Button" -> when (breakpoint) {
                Breakpoint.COMPACT -> 120.dp
                Breakpoint.MEDIUM -> 140.dp
                Breakpoint.EXPANDED -> 160.dp
            }
            "TextField" -> when (breakpoint) {
                Breakpoint.COMPACT -> 200.dp
                Breakpoint.MEDIUM -> 240.dp
                Breakpoint.EXPANDED -> 280.dp
            }
            "Card" -> when (breakpoint) {
                Breakpoint.COMPACT -> 300.dp
                Breakpoint.MEDIUM -> 350.dp
                Breakpoint.EXPANDED -> 400.dp
            }
            else -> 100.dp
        }

        val baseHeight = when (component.component) {
            "Button" -> 48.dp
            "TextField" -> 56.dp
            "Card" -> 200.dp
            else -> 40.dp
        }

        return ComponentSize(
            width = baseWidth,
            height = baseHeight,
            minWidth = baseWidth * 0.8f,
            minHeight = baseHeight * 0.8f,
            maxWidth = baseWidth * 1.5f,
            maxHeight = baseHeight * 2f
        )
    }

    /**
     * 智能组件排列
     */
    fun arrangeComponentsIntelligently(
        components: List<Component>,
        containerSize: Size,
        userBehavior: UserBehaviorData
    ): List<ComponentPlacement> {
        val frequentComponents = userBehavior.getFrequentlyUsedComponents()
        val placements = mutableListOf<ComponentPlacement>()

        // 按重要性排序组件
        val sortedComponents = components.sortedWith { a, b ->
            val aFrequent = a.id in frequentComponents
            val bFrequent = b.id in frequentComponents
            val aPriority = getComponentPriority(a)
            val bPriority = getComponentPriority(b)

            when {
                aFrequent && !bFrequent -> -1
                !aFrequent && bFrequent -> 1
                else -> bPriority.compareTo(aPriority)
            }
        }

        // 智能放置组件
        var currentY = 0f
        val containerWidth = containerSize.width

        sortedComponents.forEach { component ->
            val size = getSmartComponentSize(component, currentBreakpoint.value)
            val placement = ComponentPlacement(
                component = component,
                x = calculateOptimalX(component, containerWidth, size.width.value),
                y = currentY,
                width = size.width.value,
                height = size.height.value
            )
            placements.add(placement)
            currentY += size.height.value + layoutConfiguration.value.verticalSpacing.value
        }

        return placements
    }

    private fun getComponentPriority(component: Component): Int {
        return when (component.component) {
            "Button" -> if (component.variant == "primary") 1 else 3
            "TextField" -> 2
            "Text" -> when (component.variant) {
                "h1", "h2" -> 1
                else -> 4
            }
            "Image" -> 3
            "Card" -> 2
            else -> 5
        }
    }

    private fun calculateOptimalX(component: Component, containerWidth: Float, componentWidth: Float): Float {
        return when (component.component) {
            "Button" -> when (component.variant) {
                "primary" -> (containerWidth - componentWidth) / 2 // 居中
                else -> 16f // 左对齐
            }
            "Text" -> when (component.variant) {
                "h1", "h2", "h3" -> (containerWidth - componentWidth) / 2 // 标题居中
                else -> 16f // 正文左对齐
            }
            else -> 16f // 默认左对齐
        }
    }
}

/**
 * 断点枚举
 */
enum class Breakpoint {
    COMPACT,    // < 600dp
    MEDIUM,     // 600dp - 840dp
    EXPANDED    // > 840dp
}

/**
 * 响应式布局配置
 */
data class ResponsiveLayoutConfiguration(
    val compactColumns: Int = 1,
    val mediumColumns: Int = 2,
    val expandedColumns: Int = 3,
    val containerPadding: Dp = 16.dp,
    val gridSpacing: Dp = 12.dp,
    val verticalSpacing: Dp = 16.dp,
    val horizontalSpacing: Dp = 12.dp,
    val buttonHeight: Dp = 48.dp,
    val textFieldHeight: Dp = 56.dp,
    val cardElevation: Dp = 4.dp
)

/**
 * 组件尺寸
 */
data class ComponentSize(
    val width: Dp,
    val height: Dp,
    val minWidth: Dp,
    val minHeight: Dp,
    val maxWidth: Dp,
    val maxHeight: Dp
)

/**
 * 组件放置信息
 */
data class ComponentPlacement(
    val component: Component,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)