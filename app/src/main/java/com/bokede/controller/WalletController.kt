package com.bokede.controller

import android.content.Context
import com.bokede.controller.api.Post
import org.json.JSONObject

class WalletController(private val context: Context) {
  operator fun invoke(cookie: String, currency: String = "doge") = Post(context, "GetDepositAddress", JSONObject().put("s", cookie).put("Currency", currency))
}