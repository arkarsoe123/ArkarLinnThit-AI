package com.example.ui.screens

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import com.example.service.MusicPlaybackService

@Composable
fun MusicPlayerScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tracks by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var names by remember { mutableStateOf<List<String>>(emptyList()) }
    var current by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    var trackCount by remember { mutableIntStateOf(0) }
    var serviceTitle by remember { mutableStateOf("") }

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action != MusicPlaybackService.ACTION_STATE) return
                current = intent.getIntExtra(MusicPlaybackService.EXTRA_INDEX, current)
                playing = intent.getBooleanExtra(MusicPlaybackService.EXTRA_PLAYING, false)
                position = intent.getIntExtra(MusicPlaybackService.EXTRA_POSITION, 0)
                duration = intent.getIntExtra(MusicPlaybackService.EXTRA_DURATION, 0)
                trackCount = intent.getIntExtra(MusicPlaybackService.EXTRA_COUNT, trackCount)
                serviceTitle = intent.getStringExtra(MusicPlaybackService.EXTRA_TITLE).orEmpty()
            }
        }
    }
    DisposableEffect(Unit) {
        ContextCompat.registerReceiver(context, receiver, IntentFilter(MusicPlaybackService.ACTION_STATE), RECEIVER_NOT_EXPORTED)
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    fun service(action: String, extras: (Intent.() -> Unit)? = null) {
        val intent = Intent(context, MusicPlaybackService::class.java).setAction(action).also { extras?.invoke(it) }
        if (Build.VERSION.SDK_INT >= 26 && action != MusicPlaybackService.ACTION_STOP) context.startForegroundService(intent) else context.startService(intent)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
            tracks = uris
            names = uris.map { uri -> queryName(context, uri) }
            current = 0; position = 0; duration = 0; playing = false; trackCount = uris.size; serviceTitle = names.firstOrNull().orEmpty()
            service(MusicPlaybackService.ACTION_SET_PLAYLIST) {
                putStringArrayListExtra(MusicPlaybackService.EXTRA_URIS, ArrayList(uris.map(Uri::toString)))
                putStringArrayListExtra(MusicPlaybackService.EXTRA_NAMES, ArrayList(names))
                putExtra(MusicPlaybackService.EXTRA_INDEX, 0)
                putExtra(MusicPlaybackService.EXTRA_AUTOPLAY, false)
            }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) picker.launch(arrayOf("audio/*")) }

    fun chooseAudio() {
        val p = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED) picker.launch(arrayOf("audio/*")) else permission.launch(p)
    }

    Column(modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("သီချင်းဖွင့်စက်", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(if (tracks.isEmpty()) "Background playback အသင့်" else "${current + 1} / ${tracks.size} · Background playback")
            }
            Button(onClick = ::chooseAudio) { Icon(Icons.Default.AudioFile, null); Spacer(Modifier.size(6.dp)); Text("သီချင်းရွေးမည်") }
        }
        Spacer(Modifier.height(24.dp))
        Surface(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().weight(1f).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))) {
            Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(150.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AudioFile, null, modifier = Modifier.size(72.dp)) } }
                Spacer(Modifier.height(20.dp))
                Text(names.getOrNull(current) ?: serviceTitle.ifBlank { "သီချင်းမရွေးရသေးပါ" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (trackCount == 0) "ဖုန်းထဲက audio ဖိုင်ကို ရွေးပါ" else "App ပိတ်ပြီးနောက်လည်း ဆက်ဖွင့်နိုင်သည်")
                Spacer(Modifier.height(22.dp))
                Slider(value = if (duration > 0) position.toFloat() / duration else 0f, onValueChange = { value -> if (duration > 0) { position = (value * duration).toInt() } }, onValueChangeFinished = { service(MusicPlaybackService.ACTION_SEEK) { putExtra(MusicPlaybackService.EXTRA_SEEK, position) } }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(formatTime(position)); Text(formatTime(duration)) }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { if (trackCount > 0) service(MusicPlaybackService.ACTION_PREVIOUS) }) { Icon(Icons.Default.SkipPrevious, "ယခင်") }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(68.dp).clickable { if (trackCount > 0) service(MusicPlaybackService.ACTION_PLAY_PAUSE) }) { Box(contentAlignment = Alignment.Center) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", modifier = Modifier.size(36.dp)) } }
                    IconButton(onClick = { if (trackCount > 0) service(MusicPlaybackService.ACTION_NEXT) }) { Icon(Icons.Default.SkipNext, "နောက်") }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Text("ပင်မသို့ ပြန်သွားမည်") }
    }
}

private fun queryName(context: Context, uri: Uri): String = runCatching {
    context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(0) else uri.lastPathSegment ?: "Audio" } ?: (uri.lastPathSegment ?: "Audio")
}.getOrDefault("Audio")

private fun formatTime(ms: Int): String = "%02d:%02d".format(ms / 60000, (ms / 1000) % 60)
