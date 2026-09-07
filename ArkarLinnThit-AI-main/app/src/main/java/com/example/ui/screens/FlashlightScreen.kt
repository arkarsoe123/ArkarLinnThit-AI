package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlashlightScreen(
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var screenLightBrightness by remember { mutableFloatStateOf(0.8f) }
    var selectedLightColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var isSosMode by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "torch_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ဖုန်းမီး & မီးအလင်း ထိန်းချုပ်မှု",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = "Hardware Torch & Screen Light Matrix",
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00F0FF))
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Big Futuristic Torch Power Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clickable { onToggleTorch() }
                .testTag("torch_power_button")
        ) {
            if (isTorchOn) {
                // Pulsing light beam aura
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFF59E0B).copy(alpha = 0.6f),
                                    Color(0xFFF59E0B).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isTorchOn) Color(0xFFF59E0B) else Color(0xFF1E293B),
                shadowElevation = if (isTorchOn) 16.dp else 4.dp,
                modifier = Modifier
                    .size(130.dp)
                    .border(
                        3.dp,
                        if (isTorchOn) Color.White else Color(0xFF334155),
                        CircleShape
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Torch State",
                        tint = if (isTorchOn) Color.Black else Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isTorchOn) "ဖုန်းမီး ဖွင့်ထားပါသည် (ON)" else "ဖုန်းမီး ပိတ်ထားပါသည် (OFF)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isTorchOn) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Screen Lighting Color Matrix & Controls
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Light", tint = Color(0xFF00F0FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "မျက်နှာပြင် အလင်းရောင် ရွေးချယ်မှု (Screen Light)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color choices
                val colors = listOf(
                    Color.White to "အဖြူရောင်",
                    Color(0xFF00F0FF) to "ဆိုက်ဘာ စိမ်းပြာ",
                    Color(0xFFF59E0B) to "နွေးထွေး အဝါ",
                    Color(0xFFF43F5E) to "အနီရောင်"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { (c, label) ->
                        val isSelected = selectedLightColor == c
                        Surface(
                            shape = CircleShape,
                            color = c,
                            modifier = Modifier
                                .size(38.dp)
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) Color.Black else Color.Gray,
                                    CircleShape
                                )
                                .clickable { selectedLightColor = c }
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "အလင်းအား ပမာဏ: ${(screenLightBrightness * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = screenLightBrightness,
                    onValueChange = { screenLightBrightness = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00F0FF),
                        activeTrackColor = Color(0xFF00F0FF)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SOS Emergency Strobe toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isSosMode = !isSosMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSosMode) Color(0xFFF43F5E) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "SOS", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isSosMode) "SOS ဖွင့်နေသည်" else "အရေးပေါ် SOS Strobe")
                    }

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "ပင်မစာမျက်နှာသို့", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
