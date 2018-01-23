package com.jonlatane.beatpad.output.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.jonlatane.beatpad.MainActivity
import com.jonlatane.beatpad.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


class PlaybackService : Service(), AnkoLogger {
	companion object {
		private const val SERVICE_ID = 101

		object Action {
			const val MAIN_ACTION = "main"
			const val STARTFOREGROUND_ACTION = "start"
			const val PREV_ACTION = "prev"
			const val NEXT_ACTION = "next"
			const val STOPFOREGROUND_ACTION = "stop"
			const val PAUSE_ACTION = "pause"
			const val PLAY_ACTION = "play"
		}

		var instance: PlaybackService? = null
			private set
	}

	override fun onCreate() {
		super.onCreate()
		instance = this
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		if (intent.action == Action.STARTFOREGROUND_ACTION) {
			info("Received Start Foreground Intent ")
			showNotification()
		} else if (intent.action == Action.PREV_ACTION) {
			info("Clicked Previous")
		} else if (intent.action == Action.PLAY_ACTION) {
			info("Clicked Play")
		} else if (intent.action == Action.NEXT_ACTION) {
			info("Clicked Next")
		} else if (intent.action == Action.STOPFOREGROUND_ACTION) {
			info("Received Stop Foreground Intent")
			stopForeground(true)
			stopSelf()
		}
		return START_STICKY
	}

	override fun onDestroy() {
		super.onDestroy()
		info("In onDestroy")
	}

	override fun onBind(intent: Intent): IBinder? {
		// Used only in case of bound services.
		return null
	}

	private fun showNotification() {
		val notificationIntent = Intent(this, MainActivity::class.java)
		notificationIntent.action = Action.MAIN_ACTION
		notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		val pendingIntent = PendingIntent.getActivity(this, 0,
			notificationIntent, 0)

		val previousIntent = Intent(this, PlaybackService::class.java)
		previousIntent.action = Action.PREV_ACTION
		val ppreviousIntent = PendingIntent.getService(this, 0,
			previousIntent, 0)

		val playIntent = Intent(this, PlaybackService::class.java)
		playIntent.action = Action.PLAY_ACTION
		val pplayIntent = PendingIntent.getService(this, 0,
			playIntent, 0)

		val nextIntent = Intent(this, PlaybackService::class.java)
		nextIntent.action = Action.NEXT_ACTION
		val pnextIntent = PendingIntent.getService(this, 0,
			nextIntent, 0)

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
			.setContentTitle("Playing Audio")
			.setTicker("Playing Audio")
			.setContentText("My Palette")
			.setSmallIcon(R.mipmap.ic_launcher_round)
			.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
			.setContentIntent(pendingIntent)
			.setOngoing(true)
			.addAction(android.R.drawable.ic_media_previous, "Previous",
				ppreviousIntent)
			.addAction(android.R.drawable.ic_media_play, "Play",
				pplayIntent)
			.addAction(android.R.drawable.ic_media_next, "Next",
				pnextIntent).build()
		startForeground(SERVICE_ID, notification)

	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createNotificationChannel(): String{
		val channelId = "my_service"
		val channelName = "My Background Service"
		val chan = NotificationChannel(channelId,
			channelName, NotificationManager.IMPORTANCE_HIGH)
		chan.lightColor = Color.BLUE
		chan.importance = NotificationManager.IMPORTANCE_NONE
		chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
		val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		service.createNotificationChannel(chan)
		return channelId
	}
}