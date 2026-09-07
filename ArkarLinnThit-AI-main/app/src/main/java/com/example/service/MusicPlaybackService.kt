package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlaybackService : Service() {
    companion object {
        const val ACTION_SET_PLAYLIST = "com.aistudio.arkarlin.SET_PLAYLIST"
        const val ACTION_PLAY_PAUSE = "com.aistudio.arkarlin.PLAY_PAUSE"
        const val ACTION_PLAY = "com.aistudio.arkarlin.PLAY"
        const val ACTION_PAUSE = "com.aistudio.arkarlin.PAUSE"
        const val ACTION_NEXT = "com.aistudio.arkarlin.NEXT"
        const val ACTION_PREVIOUS = "com.aistudio.arkarlin.PREVIOUS"
        const val ACTION_SEEK = "com.aistudio.arkarlin.SEEK"
        const val ACTION_STOP = "com.aistudio.arkarlin.STOP"
        const val ACTION_STATE = "com.aistudio.arkarlin.MUSIC_STATE"
        const val EXTRA_URIS = "uris"
        const val EXTRA_NAMES = "names"
        const val EXTRA_INDEX = "index"
        const val EXTRA_AUTOPLAY = "autoplay"
        const val EXTRA_SEEK = "seek"
        const val EXTRA_PLAYING = "playing"
        const val EXTRA_POSITION = "position"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_TITLE = "title"
        const val EXTRA_COUNT = "count"
        private const val CHANNEL_ID = "arkar_music"
        private const val NOTIFICATION_ID = 4101
    }

    private var player: MediaPlayer? = null
    private var uris = mutableListOf<String>()
    private var names = mutableListOf<String>()
    private var current = 0
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        restorePlaylist()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("အာကာလင်းသစ် AI · Music", false))
        job = CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isActive) {
                broadcastState()
                delay(500)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SET_PLAYLIST -> {
                uris = intent.getStringArrayListExtra(EXTRA_URIS)?.toMutableList() ?: mutableListOf()
                names = intent.getStringArrayListExtra(EXTRA_NAMES)?.toMutableList() ?: mutableListOf()
                savePlaylist()
                current = intent.getIntExtra(EXTRA_INDEX, 0).coerceIn(0, (uris.size - 1).coerceAtLeast(0))
                loadCurrent(intent.getBooleanExtra(EXTRA_AUTOPLAY, false))
            }
            ACTION_PLAY_PAUSE -> togglePlay()
            ACTION_PLAY -> runCatching { player?.start() }.also { broadcastState(); updateNotification() }
            ACTION_PAUSE -> runCatching { player?.pause() }.also { broadcastState(); updateNotification() }
            ACTION_NEXT -> move(1)
            ACTION_PREVIOUS -> move(-1)
            ACTION_SEEK -> intent.getIntExtra(EXTRA_SEEK, 0).let { runCatching { player?.seekTo(it) } }
            ACTION_STOP -> { stopPlayback(); stopSelf() }
        }
        return START_STICKY
    }


    private fun savePlaylist() {
        val uriJson = org.json.JSONArray().apply { uris.forEach { put(it) } }.toString()
        val nameJson = org.json.JSONArray().apply { names.forEach { put(it) } }.toString()
        getSharedPreferences("music_service", MODE_PRIVATE).edit()
            .putString("uris", uriJson)
            .putString("names", nameJson)
            .putInt("index", current)
            .apply()
    }

    private fun restorePlaylist() {
        val prefs = getSharedPreferences("music_service", MODE_PRIVATE)
        uris = runCatching {
            val a = org.json.JSONArray(prefs.getString("uris", "[]") ?: "[]")
            MutableList(a.length()) { i -> a.getString(i) }
        }.getOrDefault(mutableListOf())
        names = runCatching {
            val a = org.json.JSONArray(prefs.getString("names", "[]") ?: "[]")
            MutableList(a.length()) { i -> a.getString(i) }
        }.getOrDefault(mutableListOf())
        current = prefs.getInt("index", 0).coerceIn(0, (uris.size - 1).coerceAtLeast(0))
    }

    private fun loadCurrent(autoPlay: Boolean) {
        if (uris.isEmpty()) return
        player?.release()
        player = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build())
            setOnPreparedListener { mp -> if (autoPlay) mp.start(); broadcastState(); updateNotification() }
            setOnCompletionListener { if (uris.size > 1) move(1) else broadcastState() }
            runCatching { setDataSource(this@MusicPlaybackService, Uri.parse(uris[current])); prepareAsync() }
                .onFailure { release(); player = null; broadcastState() }
        }
        broadcastState()
    }

    private fun togglePlay() {
        val p = player ?: run { loadCurrent(true); return }
        runCatching { if (p.isPlaying) p.pause() else p.start() }
        broadcastState(); updateNotification()
    }

    private fun move(delta: Int) {
        if (uris.isEmpty()) return
        current = (current + delta + uris.size) % uris.size
        val wasPlaying = player?.isPlaying == true
        loadCurrent(wasPlaying)
    }

    private fun stopPlayback() { runCatching { player?.stop() }; player?.release(); player = null; broadcastState(); updateNotification() }

    private fun broadcastState() {
        val p = player
        sendBroadcast(Intent(ACTION_STATE).setPackage(packageName).apply {
            putExtra(EXTRA_INDEX, current)
            putExtra(EXTRA_PLAYING, p?.isPlaying == true)
            putExtra(EXTRA_POSITION, runCatching { p?.currentPosition ?: 0 }.getOrDefault(0))
            putExtra(EXTRA_DURATION, runCatching { p?.duration ?: 0 }.getOrDefault(0))
            putExtra(EXTRA_TITLE, names.getOrNull(current) ?: "Music")
            putExtra(EXTRA_COUNT, uris.size)
        })
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "ArkarLinThitAI Music", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(title: String, playing: Boolean): Notification {
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        fun action(action: String, icon: Int, label: String) = NotificationCompat.Action.Builder(icon, label, PendingIntent.getService(this, action.hashCode(), Intent(this, MusicPlaybackService::class.java).setAction(action), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)).build()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(if (playing) "ဖွင့်နေသည်" else "ခဏရပ်ထားသည်")
            .setContentIntent(open)
            .setOngoing(playing)
            .addAction(action(ACTION_PREVIOUS, android.R.drawable.ic_media_previous, "Previous"))
            .addAction(action(ACTION_PLAY_PAUSE, if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play, if (playing) "Pause" else "Play"))
            .addAction(action(ACTION_NEXT, android.R.drawable.ic_media_next, "Next"))
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(names.getOrNull(current) ?: "အာကာလင်းသစ် AI · Music", player?.isPlaying == true))
    }

    override fun onDestroy() {
        job?.cancel(); job = null
        player?.release(); player = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
