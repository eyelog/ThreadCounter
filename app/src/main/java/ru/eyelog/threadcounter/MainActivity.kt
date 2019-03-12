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
import java.util.Random
import kotlin.Exception

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

        // !!!!Здесь вход в логику!!!!
        button.setOnClickListener {

            isInternetAvailable = false

            // Проверка на наличие интернета
            // Если callback пришел "unsuccess"
            if (!isInternetAvailable){

                // Запускается уведомление ->
                builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setContentTitle("Title")
                    .setContentText("Notification text")

                notification = builder.build()
                notificationManager.notify(NOTIFICATION_ID, notification)

                thread = Thread(CustomRunnable(10))
                thread.start()
            }else{
                // Если callback пришел "success" то всё клёво!
            }
        }
    }

    @SuppressLint("HandlerLeak")
    internal inner class CustomHandler : Handler() {
        override fun handleMessage(message: Message) {

            /* * * * * * * * * * * * * * * * * * */
            // И тут предполагается резаброс реста
            /* * * * * * * * * * * * * * * * * * */

            if(!isInternetAvailable){

                // Ловим вызовы от фонового потока
                val bundle = message.data
                val doTheNet = bundle.getBoolean("do")

                // Снова ловим callback и если он пришел "success"
                if(doTheNet){
                    Log.wtf("Logcat doTheNet", " Actually!")

                    // Выключаем уведомление
                    notificationManager.cancel(NOTIFICATION_ID)
                    isInternetAvailable = true
                    isDataSent = true

                    // И занимаемся своими делами

                }else{
                    // Если callback пришел "unsuccess"
                    // Фоновый поток продолжает выдавать запросы по заданной схеме
                }
            }
        }
    }

    internal inner class CustomRunnable(var steps: Int) : Runnable {

        lateinit var message: Message
        var bundle: Bundle

        // План пауз между резабросами реста
        val sleepPlan : LongArray = longArrayOf(3000, 5000, 7000, 10000, 12000, 15000, 20000, 25000, 30000, 60000)

        init {
            customHandler = CustomHandler()
            bundle = Bundle()
        }

        override fun run() {

            // Собственно цикл запускающий резапросы в Handler-е
            for (i in 0 until steps) {
                bundle.putBoolean("do", random.nextBoolean())
                message = customHandler.obtainMessage()
                message.data = bundle
                customHandler.sendMessage(message)
                try {
                    Thread.sleep(sleepPlan[i])
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: Exception){
                    e.printStackTrace()
                }

                Log.wtf("Logcat", "Sleep on $i step, on " + sleepPlan[i] + " mlsec")
            }

            // Тут предполагается некая логика на завершение цикла резапросов
            bundle.putBoolean("do", true)
            message = customHandler.obtainMessage()
            message.data = bundle
            customHandler.sendMessage(message)
        }
    }
}


