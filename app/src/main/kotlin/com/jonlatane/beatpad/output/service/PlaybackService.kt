package com.jonlatane.beatpad.output.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class PlaybackService : Service(), AnkoLogger {

  companion object {
    private const val SERVICE_ID = 101

    object Action {
      const val MAIN_ACTION = "main"
      const val STARTFOREGROUND_ACTION = "start"
      const val STOPFOREGROUND_ACTION = "stop"
      const val PAUSE_ACTION = "pause"
      const val PLAY_ACTION = "play"
    }

    var instance: PlaybackService? = null
      private set
  }

  private lateinit var playbackThread: PlaybackThread

  override fun onCreate() {
    super.onCreate()
    instance = this
    playbackThread = PlaybackThread()
    playbackThread.start()
    AndroidMidi.ONBOARD_DRIVER.start()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    when (intent.action) {
      Action.STARTFOREGROUND_ACTION -> {
        info("Received Start Foreground Intent ")
        showNotification()
      }
      Action.PLAY_ACTION -> {
        info("Clicked Play")
        BeatClockPaletteConsumer.tickPosition = 0
        playbackThread.stopped = false
        synchronized(PlaybackThread) {
          (PlaybackThread as java.lang.Object).notify()
        }
      }
      Action.PAUSE_ACTION -> {
        info("Clicked Stop")
        playbackThread.stopped = true
      }
      Action.STOPFOREGROUND_ACTION -> {
        info("Received Stop Foreground Intent")
        stopForeground(true)
        stopSelf()
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    info("In onDestroy")
    playbackThread.terminated = true
    AudioTrackCache.releaseAll()
    AndroidMidi.ONBOARD_DRIVER.stop()
  }

  override fun onBind(intent: Intent): IBinder? {
    // Used only in case of bound services.
    return null
  }

  private fun showNotification() {
    val pendingIntent = Intent(this, PaletteEditorActivity::class.java).let {
      it.action = Action.MAIN_ACTION
      it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      PendingIntent.getActivity(this, 0, it, 0)
    }

    fun pendingIntent(action: String) = PendingIntent.getService(
      this, 0,
      Intent(this, PlaybackService::class.java).also {
        it.action = action
      }, 0)

    val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round)

    val channelId =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel()
      } else {
        // If earlier version channel ID is not used
        // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
        ""
      }
    val notification = NotificationCompat.Builder(this, channelId)
      .setContentTitle("Playback Service")
      .setTicker("Playback Service")
      .setContentText("Background audio playback enabled.")
      .setSmallIcon(R.mipmap.ic_launcher_round)
      //.setPriority()
      .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
      .setContentIntent(pendingIntent)
      .setOngoing(true)
      //.addAction(android.R.drawable.ic_media_play, "Play/Pause", pendingIntent(Action.PLAY_ACTION))
      .addAction(android.R.drawable.ic_media_next, "Exit", pendingIntent(Action.STOPFOREGROUND_ACTION))
      .build()
    startForeground(SERVICE_ID, notification)

  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(): String {
    val channelId = "audio_playback"
    val channelName = "MIDI and Hardware Audio"
    val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
    //chan.lightColor = Color.BLUE
    //chan.importance = NotificationManager.IMPORTANCE_NONE
    //chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
  }
}