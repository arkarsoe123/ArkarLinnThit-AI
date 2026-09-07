package com.example.service

import com.example.model.ActiveToolScreen
import com.example.model.DynamicUiCard
import kotlin.random.Random

data class AiExecutionResult(
    val responseText: String,
    val toolToOpen: ActiveToolScreen? = null,
    val actionCommand: String? = null,
    val dynamicCards: List<DynamicUiCard>? = null
)

object MiniOfflineAi {

    fun processQuery(prompt: String): AiExecutionResult {
        val routed = IntentRouter.route(prompt)
        if (routed != null) {
            return AiExecutionResult(
                responseText = routed.response,
                toolToOpen = routed.screen,
                actionCommand = routed.action,
                dynamicCards = routed.screen?.let { listOf(
                    DynamicUiCard(
                        cardType = "TOOL_RESULT",
                        title = it.titleMy,
                        subtitle = "Mini AI Smart Router",
                        primaryMetric = "READY",
                        secondaryMetric = it.titleEn,
                        statusText = prompt.take(80),
                        items = listOf("Intent: ${routed.action ?: "CHAT"}"),
                        accentHex = 0xFF5B8CFF
                    )
                ) }
            )
        }

        val p = prompt.trim().lowercase()
        if (p.contains("မင်္ဂလာပါ") || p.contains("hello") || p.contains("hi")) {
            return AiExecutionResult("မင်္ဂလာပါခင်ဗျာ! အာကာလင်းသစ် AI အသင့်ရှိပါတယ်။ ဘာလုပ်ပေးရမလဲ ပြောနိုင်ပါတယ်။")
        }

        if (p.contains("ဘယ်သူလဲ") || p.contains("who are you") || p.contains("နာမည်")) {
            return AiExecutionResult("ကျွန်တော်က အာကာလင်းသစ် AI ပါ။ စာ၊ အသံနဲ့ ဖုန်းထဲက Tool တွေကို ကူညီထိန်းချုပ်ပေးနိုင်ပါတယ်ခင်ဗျာ။")
        }

        val mathMatch = Regex("([0-9.]+)\\s*([+\\-*/xX])\\s*([0-9.]+)").find(p)
        if (mathMatch != null) {
            val (a, op, b) = mathMatch.destructured
            val x = a.toDoubleOrNull()
            val y = b.toDoubleOrNull()
            if (x != null && y != null) {
                val r = when (op) {
                    "+" -> x + y
                    "-" -> x - y
                    "*", "x", "X" -> x * y
                    "/" -> if (y != 0.0) x / y else Double.NaN
                    else -> Double.NaN
                }
                if (!r.isNaN()) {
                    val formatted = if (r % 1.0 == 0.0) r.toLong().toString() else "%.2f".format(r)
                    return AiExecutionResult("$a $op $b = $formatted ဖြစ်ပါတယ်ခင်ဗျာ။", dynamicCards = listOf(
                        DynamicUiCard(cardType = "CALCULATOR", title = "တွက်ချက်မှု", subtitle = "Mini AI calculator", primaryMetric = formatted, secondaryMetric = "Math", statusText = "OK", items = listOf("$a $op $b"), accentHex = 0xFF8B5CF6)
                    ))
                }
            }
        }

        return AiExecutionResult(
            "လူကြီးမင်းပြောတာကို နားလည်ဖို့ ထပ်ပြီးအသေးစိတ်ပြောပေးနိုင်ပါတယ်ခင်ဗျာ။ ဥပမာ — 'ဖုန်းမီးဖွင့်', '၅ မိနစ် timer', 'သီချင်းဖွင့်', 'Gallery ဖွင့်'။"
        )
    }

    fun generateRandomDynamicUiCards(): List<DynamicUiCard> {
        val seed = Random.nextInt(1, 1000)
        return listOf(
            DynamicUiCard(
                cardType = "METRIC_MONITOR",
                title = "ခေတ်သစ် AI စနစ် စွမ်းဆောင်ရည် #$seed",
                subtitle = "Active Neural Engine & Device Sync",
                primaryMetric = "${95 + (seed % 5)}%",
                secondaryMetric = "${120 + (seed % 30)} ms Latency",
                statusText = "စနစ်အခြေအနေ: အထူးကောင်းမွန်နေပါသည်",
                items = listOf("Offline Neural Core: အဆင်သင့်", "Voice Buffer: 100%", "Tool Integration: 14 Tools Active"),
                accentHex = 0xFF00F0FF
            ),
            DynamicUiCard(
                cardType = "SMART_SUGGESTION",
                title = "အာကာလင်းသစ် AI အကြံပြုချက်",
                subtitle = "အသုံးပြုမှုအလိုက် အလိုအလျောက် ပြင်ဆင်ချက်",
                primaryMetric = "Optimum",
                secondaryMetric = "Auto Mode",
                statusText = "Smart Dynamic Layout စနစ်အလုပ်လုပ်နေပါသည်",
                items = listOf("မနက်ခင်း စနစ် အလိုအလျောက် ဖွင့်ရန်", "မျက်စိအေးစေမည့် Dark Mode သို့ ပြောင်းရန်", "Code Editor Workspace အသင့်ပြင်ရန်"),
                accentHex = 0xFF10B981
            ),
            DynamicUiCard(
                cardType = "TELEMETRY",
                title = "ဖုန်းစနစ်နှင့် အာကာလင်းသစ် Telemetry",
                subtitle = "Real-time Sensor & Hardware Dashboard",
                primaryMetric = "${32 + (seed % 8)}°C",
                secondaryMetric = "Normal Temp",
                statusText = "Memory: 4.2 GB / 8.0 GB Used",
                items = listOf("Flashlight: Ready", "Camera Engine: Standby", "Audio Visualizer: 60 FPS"),
                accentHex = 0xFF8B5CF6
            )
        )
    }
}
