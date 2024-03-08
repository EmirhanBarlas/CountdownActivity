package com.example.countdownactivity;

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.view.Window;
import android.view.WindowManager;

class MainActivity : AppCompatActivity() {

    private lateinit var countdownText: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        countdownText = findViewById(R.id.countdown_text)

        //Set target date (for example, March 28, 2024)
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val targetDate = "28-03-2024" // Target date
        try {
            val date: Date = sdf.parse(targetDate)!!
            val targetTimeInMillis: Long = date.time
            val currentTimeInMillis: Long = System.currentTimeMillis()
            timeLeftInMillis = targetTimeInMillis - currentTimeInMillis

            startCountDown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCountDown() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateCountDownText()
            }
        }.start()
    }

    private fun updateCountDownText() {
        val days: Long = TimeUnit.MILLISECONDS.toDays(timeLeftInMillis)
        val hours: Long = TimeUnit.MILLISECONDS.toHours(timeLeftInMillis) % 24
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis) % 60
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60

        val timeLeftFormatted = String.format(
            Locale.getDefault(),
            "%d gün %d saat %d dakika %d saniye",
            days, hours, minutes, seconds
        )
        countdownText.text = timeLeftFormatted

        // You can change the notification sending section every 6 hours.
        if (timeLeftInMillis > 0 && timeLeftInMillis % TimeUnit.HOURS.toMillis(6) == 0L) {
            sendNotification("Doğum gününe kalan süre: $timeLeftFormatted")
        }
    }

    private fun sendNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = Notification.Builder(this)
            .setContentTitle("Doğum Günü Bildirimi")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
