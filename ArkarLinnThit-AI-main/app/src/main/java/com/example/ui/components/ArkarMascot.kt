package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MascotEmotion
import kotlin.math.roundToInt

@Composable
fun ArkarMascot(
    emotion: MascotEmotion,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    latestSpeechText: String? = null,
    onMascotClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_anim")

    // Floating breathing bounce
    val floatingOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float"
    )

    // Glowing aura pulse
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_glow"
    )

    // Rotating cyber ring angle
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    val (auraColor, statusTitle, statusIcon) = when (emotion) {
        MascotEmotion.LISTENING -> Triple(Color(0xFF10B981), "အသံနားထောင်နေသည်...", Icons.Default.Mic)
        MascotEmotion.THINKING -> Triple(Color(0xFF8B5CF6), "AI ဦးနှောက် စဉ်းစားနေသည်...", Icons.Default.Psychology)
        MascotEmotion.SPEAKING -> Triple(Color(0xFF00F0FF), "အာကာလင်းသစ် ရှင်းပြနေသည်...", Icons.Default.RecordVoiceOver)
        MascotEmotion.EXCITED, MascotEmotion.SUCCESS -> Triple(Color(0xFFF59E0B), "အဆင်သင့်ဖြစ်ပါပြီ!", Icons.Default.AutoAwesome)
        MascotEmotion.ERROR -> Triple(Color(0xFFF43F5E), "သတိပြုရန်", Icons.Default.Psychology)
        MascotEmotion.IDLE -> Triple(Color(0xFF00F0FF), "အာကာလင်းသစ် AI", Icons.Default.AutoAwesome)
    }

    Column(
        modifier = modifier.fillMaxWidthModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble if mascot is speaking
        if (!latestSpeechText.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .border(1.dp, auraColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("mascot_speech_bubble")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Status",
                        tint = auraColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = latestSpeechText.take(120) + if (latestSpeechText.length > 120) "..." else "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Mascot Avatar with Animated Glow and Sleek Interface Concentric Disc
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset { IntOffset(0, floatingOffsetY.roundToInt()) }
                .clickable { onMascotClick() }
                .testTag("mascot_avatar")
        ) {
            // Ambient soft blur glow
            Box(
                modifier = Modifier
                    .size(size * 1.86f)
                    .scale(glowPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF3B82F6).copy(alpha = 0.20f),
                                Color(0xFFA855F7).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Sleek Outer Disc (w-48 h-48 = 192dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size * 1.63f)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1C1E))
                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f), CircleShape)
            ) {
                // Radial pulse highlight inside disc
                Box(
                    modifier = Modifier
                        .size(size * 1.63f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.20f * glowPulse),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Rotating cyber orbit ring
                Box(
                    modifier = Modifier
                        .size(size * 1.51f)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF3B82F6).copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color(0xFFA855F7).copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                style = Stroke(width = 1.5f)
                            )
                        }
                )

                // Inner avatar and label container
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Inner Avatar container (w-24 h-24 = 96dp)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(size * 0.81f)
                            .clip(CircleShape)
                            .background(Color(0xFF2D3135))
                            .border(2.dp, Color(0xFFA855F7).copy(alpha = 0.35f), CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.arkar_mascot),
                            contentDescription = "အာကာလင်းသစ် AI Mascot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(size * 0.76f)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "NEURAL CHILD BRAIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA),
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                // Emotion status badge at bottom right
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1D2024),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                        .border(1.5.dp, auraColor, CircleShape)
                        .size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Mascot Status",
                            tint = auraColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status pill title
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1D2024),
            modifier = Modifier.border(1.dp, auraColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(auraColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFE2E2E6),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}

private fun Modifier.fillMaxWidthModifier(): Modifier = this.padding(vertical = 4.dp)
