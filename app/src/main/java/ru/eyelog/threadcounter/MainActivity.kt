package ru.eyelog.threadcounter

import android.annotation.SuppressLint
import android.app.Notification
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.lang.Exception
import java.util.Random

class MainActivity : AppCompatActivity() {

    val CHANNEL_ID = "101"
    val NOTIFICATION_ID = 102
    var isInternetAvailable = false
    var isDataSent = false
    lateinit var builder : NotificationCompat.Builder
    lateinit var notification : Notification
    lateinit var thread : Thread
    lateinit var notificationManager :NotificationManager
    lateinit var random: Random
    internal lateinit var customHandler: CustomHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        random = Random()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "My channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "My channel description"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
        button.setOnClickListener {
            if (!isInternetAvailable){
                builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setContentTitle("Title")
                    .setContentText("Notification text")

                notification = builder.build()
                notificationManager.notify(NOTIFICATION_ID, notification)

                thread = Thread(CustomRunnable(random.nextInt(10)))
                thread.start()
            }else{
                // Do nice logic
            }
        }
    }

    @SuppressLint("HandlerLeak")
    internal inner class CustomHandler : Handler() {
        override fun handleMessage(message: Message) {

            // Here we actually try to send the data

            val bundle = message.data
            val doTheNet = bundle.getBoolean("do")
            if(doTheNet){
                Log.wtf("Logcat doTheNet", " Actually!")

                notificationManager.cancel(NOTIFICATION_ID)

                // And do send logic

                isDataSent = true
            }else{
                // Some sad notification =(
            }
        }
    }

    internal inner class CustomRunnable(var steps: Int) : Runnable {

        lateinit var message: Message
        var bundle: Bundle

        init {
            customHandler = CustomHandler()
            bundle = Bundle()
        }

        override fun run() {
            for (i in 0 until steps) {
                bundle.putBoolean("do", random.nextBoolean())
                message = customHandler.obtainMessage()
                message.data = bundle
                customHandler.sendMessage(message)
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                if(thread.isInterrupted()){
                    break
                }

                Log.wtf("Logcat", "i = $i, steps = $steps")
            }

            bundle.putBoolean("do", true)
            message = customHandler.obtainMessage()
            message.data = bundle
            customHandler.sendMessage(message)
        }
    }
}


