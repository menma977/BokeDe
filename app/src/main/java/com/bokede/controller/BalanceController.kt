package com.bokede.controller

import android.content.Context
import com.bokede.controller.api.Post
import org.json.JSONObject

class BalanceController(private val context: Context) {
  operator fun invoke(cookie: String, currency: String = "doge") = Post(context, "GetBalance", JSONObject().put("s", cookie).put("Currency", currency).put("Stats", 1))
}