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
import android.support.v4.app.NotificationCompat.PRIORITY_MAX
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.widget.RemoteViews
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class PlaybackService : Service(), AnkoLogger {

  companion object {
    private const val SERVICE_ID = 101

    object Action {
      const val MAIN_ACTION = "main"
      const val STARTFOREGROUND_ACTION = "startService"
      const val STOPFOREGROUND_ACTION = "stopService"
      const val PLAY_ACTION = "play"
      const val PAUSE_ACTION = "pause"
      const val STOP_ACTION = "stop"
      const val REWIND_ACTION = "rewind"
    }

    var instance: PlaybackService? = null
      private set
  }

  private lateinit var playbackThread: PlaybackThread
  val isStopped get() = playbackThread.stopped

  override fun onCreate() {
    super.onCreate()
    instance = this
    playbackThread = PlaybackThread()
    playbackThread.start()
    AndroidMidi.ONBOARD_DRIVER.start()
    MidiDevices.refreshInstruments()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      Action.STARTFOREGROUND_ACTION -> {
        info("Received Start Foreground Intent ")
        showNotification()
      }
      Action.PLAY_ACTION            -> {
        info("Clicked Play")
        playbackThread.stopped = false
        synchronized(PlaybackThread) {
          (PlaybackThread as java.lang.Object).notify()
        }
        showNotification()
      }
      Action.REWIND_ACTION            -> {
        info("Clicked Rewind")
        BeatClockPaletteConsumer.tickPosition = 0
      }
      Action.PAUSE_ACTION           -> {
        info("Clicked Pause")
        playbackThread.stopped = true
        showNotification()
      }
      Action.STOP_ACTION            -> {
        info("Clicked Stop")
        playbackThread.stopped = true
        BeatClockPaletteConsumer.tickPosition = 0
        BeatClockPaletteConsumer.viewModel?.playbackTick = 0
        showNotification()
      }
      Action.STOPFOREGROUND_ACTION  -> {
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

  fun showNotification() {
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

//    val icon = BitmapFactory.decodeResource(resources, R.drawable.beatscratch_icon)

    val channelId =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel()
      } else {
        // If earlier version channel ID is not used
        // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
        ""
      }

    val sectionName = BeatClockPaletteConsumer.section?.name ?: "..."
    val notification = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(R.drawable.beatscratch_icon_notification_inset_slight)
      .setContentTitle("MIDI Playback")
      .setTicker("MIDI Playback")
      .setPriority(PRIORITY_MAX)
      .setContentText(sectionName)
      //.setPriority()
//      .setL
//      .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
      .setContentIntent(pendingIntent)
      .setOngoing(true)
      .apply {
        if(isStopped) {
          addAction(R.drawable.play_notification, "Play", pendingIntent(Action.PLAY_ACTION))
        } else {
          addAction(R.drawable.previous_notification, "Skip back", pendingIntent(Action.REWIND_ACTION))
        }
      }
      .addAction(R.drawable.stop_notification, "Stop", pendingIntent(Action.STOP_ACTION))
      .addAction(R.drawable.close_notification, "Exit", pendingIntent(Action.STOPFOREGROUND_ACTION))
      .setStyle(
        MediaStyle()
          .setShowActionsInCompactView(0, 1)
//          .setShowCancelButton(true)
//          .setCancelButtonIntent(pendingIntent(Action.STOPFOREGROUND_ACTION))
      )
      .build()
    startForeground(SERVICE_ID, notification)

  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(): String {
    val channelId = "audio_playback"
    val channelName = "MIDI and Hardware Audio"
    val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
    //chan.lightColor = Color.BLUE
    //chan.importance = NotificationManager.IMPORTANCE_NONE
    //chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
  }
}