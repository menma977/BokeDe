package com.bokede.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bokede.controller.BalanceController
import com.bokede.controller.api.HandleError
import com.bokede.controller.api.HandleResult
import com.bokede.model.User
import kotlinx.coroutines.*

class Balance : Service() {
  private lateinit var user: User
  private lateinit var job: CompletableJob
  private var service: Boolean = false

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  private fun onHandleIntent() {
    if (!::job.isInitialized || job.isCompleted) {
      job = Job()
    }

    user = User(this)

    runService()
  }

  override fun onCreate() {
    super.onCreate()
    service = true
    onHandleIntent()
  }

  override fun onDestroy() {
    super.onDestroy()
    service = false
    job.cancel()
  }

  private fun runService() {
    val privateIntent = Intent()
    var time = System.currentTimeMillis()
    var dynamicTime = 1000
    CoroutineScope(Dispatchers.IO + job).launch {
      while (isActive) {
        val delta = System.currentTimeMillis() - time
        if (service) {
          if (delta >= dynamicTime) {
            time = System.currentTimeMillis()
            try {
              BalanceController(applicationContext).invoke(user.getString("cookie"), "doge").cll({
                val response = HandleResult(it).result()
                if (response.getInt("code") < 400) {
                  privateIntent.putExtra("balance", response.getJSONObject("data").getString("Balance"))
                  privateIntent.putExtra("payIn", response.getJSONObject("data").getString("TotalPayIn"))
                  privateIntent.putExtra("payOut", response.getJSONObject("data").getString("TotalPayOut"))
                  privateIntent.putExtra("bet", response.getJSONObject("data").getString("TotalBets"))
                  privateIntent.putExtra("win", response.getJSONObject("data").getString("TotalWins"))
                  privateIntent.action = "doge.balance"
                  LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
                  dynamicTime = 8000
                } else {
                  Log.e("error Service Balance", response.getString("data"))
                  dynamicTime = 60000
                }
              }, {
                val response = HandleError(it).result()
                Log.e("error Service Balance", response.getString("message"))
                dynamicTime = 60000
              })
            } catch (e: Exception) {
              Log.e("error Service Balance", e.message.toString())
              dynamicTime = 60000
            }
          }
        } else {
          stopSelf()
          job.cancel()
        }
      }
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    TODO("Not yet implemented")
  }
}