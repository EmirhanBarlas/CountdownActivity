package com.example.countdownactivity

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var birthdayPicker: DatePicker
    private lateinit var confirmButton: Button
    private lateinit var countdownText: TextView
    private lateinit var ageText: TextView
    private lateinit var resetButton: Button
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private lateinit var targetDate: String // Doğum günü tarihi
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("BirthdayPrefs", Context.MODE_PRIVATE)

        if (sharedPreferences.contains("targetDate")) {
            targetDate = sharedPreferences.getString("targetDate", "")!!
            if (isBirthdayPassed(targetDate)) {
                val nextBirthday = getNextBirthday(targetDate)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val nextBirthdayString = sdf.format(nextBirthday.time)
                setTargetDate(nextBirthdayString)
            }
            setContentView(R.layout.activity_countdown)
            initCountdown()
        } else {
            setContentView(R.layout.activity_main)
            initBirthdayPicker()
        }
    }

    private fun getNextBirthday(date: String): Calendar {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentCalendar = Calendar.getInstance()
        val targetCalendar = Calendar.getInstance()

        val targetDate = sdf.parse(date) ?: return currentCalendar
        targetCalendar.time = targetDate

        if (targetCalendar.get(Calendar.MONTH) < currentCalendar.get(Calendar.MONTH) ||
            (targetCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    targetCalendar.get(Calendar.DAY_OF_MONTH) < currentCalendar.get(Calendar.DAY_OF_MONTH))) {
            targetCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR) + 1)
        } else {
            targetCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
        }

        return targetCalendar
    }

    private fun initBirthdayPicker() {
        birthdayPicker = findViewById(R.id.birthday_picker)
        confirmButton = findViewById(R.id.confirm_button)
        countdownText = findViewById(R.id.countdown_text)

        if (birthdayPicker != null && confirmButton != null && countdownText != null) {
            // Onay düğmesine tıklandığında doğum gününü al
            confirmButton.setOnClickListener {
                // Doğum günü hesaplanıyor mesajı ekle
                countdownText.text = "Doğum gününüz hesaplanıyor..."

                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    getBirthdayAndStartCountdown()
                }
            }
        } else {
            Log.e(TAG, "initBirthdayPicker: Görünümler null.")
        }
    }

    private fun getBirthdayAndStartCountdown() {
        val day = birthdayPicker.dayOfMonth
        val month = birthdayPicker.month
        val year = birthdayPicker.year

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year)
        setTargetDate(selectedDate)

        sharedPreferences.edit().putString("targetDate", targetDate).apply()

        initCountdown()
    }

    private fun initCountdown() {
        setContentView(R.layout.activity_countdown)
        countdownText = findViewById(R.id.countdown_text)
        resetButton = findViewById(R.id.reset_button)

        resetButton.setOnClickListener {
            sharedPreferences.edit().remove("targetDate").apply()
            recreate()
        }

        startCountDown()
    }

    private fun isBirthdayPassed(date: String): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val targetDate = sdf.parse(date) ?: return false
        return currentDate.after(targetDate)
    }
    private fun setTargetDate(date: String) {
        this.targetDate = date
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        try {
            val targetDateTimeInMillis: Long = sdf.parse(targetDate)!!.time
            val currentDateTimeInMillis: Long = System.currentTimeMillis()
            timeLeftInMillis = targetDateTimeInMillis - currentDateTimeInMillis

            val birthYear = sdf.parse(targetDate)?.year ?: 0
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val age = currentYear - birthYear
            ageText.text = "Yaşınız: $age"
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

        val timeLeftFormatted = when {
            days > 0 -> String.format(
                Locale.getDefault(),
                "%d gün %02d saat %02d dakika %02d saniye",
                days, hours, minutes, seconds
            )
            hours > 0 -> String.format(
                Locale.getDefault(),
                "%02d saat %02d dakika %02d saniye",
                hours, minutes, seconds
            )
            minutes > 0 -> String.format(
                Locale.getDefault(),
                "%02d dakika %02d saniye",
                minutes, seconds
            )
            else -> String.format(
                Locale.getDefault(),
                "%02d saniye",
                seconds
            )
        }

        findViewById<TextView>(R.id.countdown_text).text = timeLeftFormatted
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
