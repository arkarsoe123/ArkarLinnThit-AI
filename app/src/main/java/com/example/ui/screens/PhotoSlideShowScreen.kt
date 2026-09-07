package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PhotoSlideShowScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var autoPlay by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            photos = uris
            currentIndex = 0
            uris.forEach { uri ->
                runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            }
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted || Build.VERSION.SDK_INT >= 33) picker.launch(arrayOf("image/*"))
        else picker.launch(arrayOf("image/*"))
    }

    fun choosePhotos() {
        if (Build.VERSION.SDK_INT >= 33) {
            val p = Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED) picker.launch(arrayOf("image/*"))
            else permission.launch(p)
        } else {
            val p = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED) picker.launch(arrayOf("image/*"))
            else permission.launch(p)
        }
    }

    LaunchedEffect(autoPlay, photos.size) {
        while (autoPlay && photos.isNotEmpty()) {
            kotlinx.coroutines.delay(3500)
            currentIndex = (currentIndex + 1) % photos.size
        }
    }

    val imageUri = photos.getOrNull(currentIndex)
    val bitmap by androidx.compose.runtime.produceState<android.graphics.Bitmap?>(initialValue = null, imageUri) {
        value = imageUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) }.getOrNull()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ဓာတ်ပုံ ဆလိုက်ရှိုး", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    if (photos.isEmpty()) "ဖုန်းထဲက ဓာတ်ပုံကို ရွေးပါ" else "${currentIndex + 1} / ${photos.size} · ဖုန်းထဲက အမှန်တကယ်ဓာတ်ပုံ",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button(onClick = ::choosePhotos) {
                Icon(Icons.Default.Collections, null)
                Spacer(Modifier.width(6.dp))
                Text("ဓာတ်ပုံရွေးမည်")
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.weight(1f).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)).testTag("slideshow_viewport")
        ) {
            Crossfade(targetState = bitmap, animationSpec = tween(400), label = "photo") { bmp ->
                Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (bmp != null) {
                        Image(bmp.asImageBitmap(), "ရွေးထားသောဓာတ်ပုံ", Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Fit)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Collections, null, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(if (photos.isEmpty()) "ဓာတ်ပုံများ မရွေးရသေးပါ" else "ဓာတ်ပုံဖတ်နေသည်…")
                            Text("Permission ပေးပြီး ဓာတ်ပုံရွေးပါ", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (photos.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(photos) { index, uri ->
                    val thumb by androidx.compose.runtime.produceState<android.graphics.Bitmap?>(null, uri) {
                        value = withContext(Dispatchers.IO) { runCatching { context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) }.getOrNull() }
                    }
                    Box(
                        Modifier.size(64.dp, 48.dp).clip(RoundedCornerShape(8.dp)).border(if (index == currentIndex) 2.dp else 1.dp, if (index == currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).clickable { currentIndex = index }
                    ) {
                        thumb?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (photos.isNotEmpty()) currentIndex = if (currentIndex == 0) photos.lastIndex else currentIndex - 1 }) {
                Icon(Icons.Default.NavigateBefore, "ယခင်")
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(58.dp).clickable { if (photos.isNotEmpty()) autoPlay = !autoPlay }.testTag("slideshow_autoplay_toggle")) {
                Box(contentAlignment = Alignment.Center) { Icon(if (autoPlay) Icons.Default.Pause else Icons.Default.PlayArrow, "အလိုအလျောက်", modifier = Modifier.size(28.dp)) }
            }
            IconButton(onClick = { if (photos.isNotEmpty()) currentIndex = (currentIndex + 1) % photos.size }) {
                Icon(Icons.Default.NavigateNext, "နောက်တစ်ပုံ")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Text("ပင်မသို့ ပြန်သွားမည်") }
    }
}
