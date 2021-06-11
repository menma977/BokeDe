package com.bokede.controller

import android.content.Context
import com.bokede.controller.api.Post
import org.json.JSONObject

class WithdrawController(private val context: Context) {
  operator fun invoke(cookie: String, amount: String, wallet: String) = Post(
    context,
    "Withdraw",
    JSONObject()
      .put("s", cookie)
      .put("Amount", amount)
      .put("Address", wallet)
      .put("Currency", "doge")
  )
}