package com.bokede.controller

import android.content.Context
import com.bokede.controller.api.Post
import org.json.JSONObject

class BotController(private val context: Context) {

  fun manual(
    cookie: String,
    payIn: String,
    low: String,
    high: String,
    seed: String
  ) = Post(
    context,
    "PlaceBet",
    JSONObject()
      .put("s", cookie)
      .put("PayIn", payIn)
      .put("Low", low)
      .put("High", high)
      .put("ClientSeed", seed)
      .put("Currency", "doge")
      .put("ProtocolVersion", "2")
  )

}