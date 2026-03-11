package org.a2ui.compose.ai.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.a2ui.compose.ai.*
import org.a2ui.compose.rendering.A2UIRenderer

/**
 * AI布局优化演示应用
 *
 * 展示AI驱动的布局优化功能：
 * 1. 实时布局分析
 * 2. 智能响应式设计
 * 3. 无障碍自动增强
 * 4. 用户行为学习
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AILayoutOptimizationDemo() {
    val renderer = remember { A2UIRenderer() }
    val aiSystem = rememberAILayoutOptimizationSystem(renderer)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("布局优化", "分析报告", "无障碍", "设置")

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部应用栏
        TopAppBar(
            title = { Text("AI布局优化演示") },
            actions = {
                IconButton(onClick = { /* 设置 */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
            }
        )

        // 标签页
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // 内容区域
        when (selectedTab) {
            0 -> LayoutOptimizationTab(aiSystem)
            1 -> AnalysisReportTab(aiSystem)
            2 -> AccessibilityTab(aiSystem)
            3 -> SettingsTab(aiSystem)
        }
    }
}

/**
 * 布局优化标签页
 */
@Composable
private fun LayoutOptimizationTab(aiSystem: AILayoutOptimizationSystem) {
    val breakpoint by aiSystem.getCurrentBreakpoint().collectAsState()
    val smartBreakpoints = aiSystem.getSmartBreakpoints()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 当前断点信息
            InfoCard(
                title = "当前断点",
                icon = Icons.Default.DeviceHub,
                content = {
                    Column {
                        Text("断点: ${breakpoint.name}")
                        Text("推荐列数: ${smartBreakpoints.recommendedColumns}")
                        Text("推荐间距: ${smartBreakpoints.recommendedSpacing}")
                    }
                }
            )
        }

        item {
            // AI优化的演示界面
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI优化演示界面",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 使用AI优化的Surface
                    aiSystem.AIOptimizedSurface(
                        surfaceId = "demo_surface",
                        modifier = Modifier.height(400.dp)
                    )
                }
            }
        }

        item {
            // 交互按钮区域
            InteractionButtonsSection(aiSystem)
        }
    }
}

/**
 * 分析报告标签页
 */
@Composable
private fun AnalysisReportTab(aiSystem: AILayoutOptimizationSystem) {
    val optimizationResults by aiSystem.optimizationResults.collectAsState()
    val stats = aiSystem.getOptimizationStats()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 优化统计
            InfoCard(
                title = "优化统计",
                icon = Icons.Default.Analytics,
                content = {
                    Column {
                        StatRow("总优化次数", stats.totalOptimizations.toString())
                        StatRow("平均评分", "${String.format("%.1f", stats.averageScore * 100)}%")
                        StatRow("用户交互", stats.totalInteractions.toString())
                        StatRow("会话时长", "${stats.sessionDuration / 1000}秒")
                    }
                }
            )
        }

        optimizationResults?.let { results ->
            item {
                // 布局分析结果
                InfoCard(
                    title = "布局分析",
                    icon = Icons.Default.Assessment,
                    content = {
                        Column {
                            StatRow("整体评分", "${String.format("%.1f", results.analysisResult.overallScore * 100)}%")
                            StatRow("视觉平衡", "${String.format("%.1f", results.analysisResult.visualBalance * 100)}%")
                            StatRow("认知负荷", "${String.format("%.1f", results.analysisResult.cognitiveLoad * 100)}%")
                            StatRow("无障碍性", "${String.format("%.1f", results.analysisResult.accessibilityScore * 100)}%")
                            StatRow("可用性", "${String.format("%.1f", results.analysisResult.usabilityScore * 100)}%")
                        }
                    }
                )
            }

            item {
                // 优化建议
                InfoCard(
                    title = "优化建议",
                    icon = Icons.Default.Lightbulb,
                    content = {
                        Column {
                            results.analysisResult.suggestions.forEach { suggestion ->
                                SuggestionItem(suggestion)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * 无障碍标签页
 */
@Composable
private fun AccessibilityTab(aiSystem: AILayoutOptimizationSystem) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InfoCard(
                title = "无障碍增强",
                icon = Icons.Default.Accessibility,
                content = {
                    Text("AI系统会自动检测并增强界面的无障碍性，包括：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Text("• 自动生成语义化标签")
                        Text("• 优化颜色对比度")
                        Text("• 调整触摸目标大小")
                        Text("• 适配屏幕阅读器")
                        Text("• 支持大字体模式")
                    }
                }
            )
        }

        item {
            Button(
                onClick = {
                    // 应用自动修复
                    // TODO: Fix coroutine scope usage
                    // kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    //     aiSystem.applyAutoFixes("demo_surface")
                    // }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("应用无障碍自动修复")
            }
        }
    }
}

/**
 * 设置标签页
 */
@Composable
private fun SettingsTab(aiSystem: AILayoutOptimizationSystem) {
    val isOptimizationEnabled by aiSystem.isOptimizationEnabled.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI优化设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("启用AI优化")
                        Switch(
                            checked = isOptimizationEnabled,
                            onCheckedChange = { aiSystem.setOptimizationEnabled(it) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 交互按钮区域
 */
@Composable
private fun InteractionButtonsSection(aiSystem: AILayoutOptimizationSystem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "模拟用户交互",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        aiSystem.recordUserInteraction(
                            componentId = "button_1",
                            action = "click",
                            duration = 500L
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("按钮点击")
                }

                Button(
                    onClick = {
                        aiSystem.recordUserInteraction(
                            componentId = "text_field_1",
                            action = "focus",
                            duration = 2000L
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("文本输入")
                }
            }
        }
    }
}

/**
 * 信息卡片组件
 */
@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

/**
 * 统计行组件
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 建议项组件
 */
@Composable
private fun SuggestionItem(suggestion: SmartOptimizationSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (suggestion.priority) {
                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (suggestion.priority) {
                        Priority.HIGH -> Icons.Default.Warning
                        Priority.MEDIUM -> Icons.Default.Info
                        else -> Icons.Default.Lightbulb
                    },
                    contentDescription = null,
                    tint = when (suggestion.priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error
                        Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "建议: ${suggestion.action}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}