package com.example.countdownactivity

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var countdownText: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        countdownText = findViewById(R.id.countdown_text)

        // Hedef tarihi ayarla (örneğin, 31 Aralık 2024)
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val targetDate = "31-12-2024" // Hedef tarih
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
