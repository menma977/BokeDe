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

  override fun onBind(intent: Intent?): IBinder? {
    TODO("Not yet implemented")
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  private fun onHandleIntent() {
    if (!::job.isInitialized || job.isCompleted) {
      job = Job()
    }

    user = User(this)

    CoroutineScope(Dispatchers.IO + job).launch {
      runService()
    }
  }

  override fun onCreate() {
    super.onCreate()
    onHandleIntent()
    service = true
  }

  override fun onDestroy() {
    super.onDestroy()
    service = false
    job.cancel()
  }

  private fun runService() {
    val privateIntent = Intent()
    Log.i("status balance service", service.toString())
    if (service) {
      try {
        BalanceController(this).invoke(user.getString("cookie"), "doge").cll({
          val response = HandleResult(it).result()
          if (response.getInt("code") < 400) {
            Log.i("response", response.getJSONObject("data").toString())
            privateIntent.putExtra("balance", response.getJSONObject("data").getString("Balance"))
            privateIntent.putExtra("payIn", response.getJSONObject("data").getString("TotalPayIn"))
            privateIntent.putExtra("payOut", response.getJSONObject("data").getString("TotalPayOut"))
            privateIntent.putExtra("bet", response.getJSONObject("data").getString("TotalBets"))
            privateIntent.putExtra("win", response.getJSONObject("data").getString("TotalWins"))
            privateIntent.action = "doge.balance"
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(privateIntent)
            GlobalScope.launch {
              delay(10000)
              runService()
            }
          } else {
            Log.e("error Service Balance", response.getString("data"))
            GlobalScope.launch {
              delay(60000)
              runService()
            }
          }
        }, {
          val response = HandleError(it).result()
          Log.e("error Service Balance", response.getString("data"))
          GlobalScope.launch {
            delay(60000)
            runService()
          }
        })
      } catch (e: Exception) {
        Log.e("error Service Balance", e.message.toString())
      }
    }
  }
}