package org.a2ui.compose.protocol

import org.junit.Assert.assertTrue
import org.junit.Test

class A2UIPromptGuidanceTest {

    @Test
    fun toolSummaryInstruction_coversCommonDynamicSceneCards() {
        val prompt = A2UIPromptGuidance.toolSummaryInstruction()

        listOf("天气", "路线", "地点", "票务", "媒体", "车辆", "诊断", "表单", "任务").forEach { keyword ->
            assertTrue("missing keyword: $keyword", prompt.contains(keyword))
        }
        assertTrue(prompt.contains("A2UI JSONL"))
        assertTrue(prompt.contains("不要把原始 JSON"))
        assertTrue(prompt.contains("自然语言"))
    }
}
