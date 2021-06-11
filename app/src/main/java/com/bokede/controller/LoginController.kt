package com.bokede.controller

import android.content.Context
import com.bokede.controller.api.Post
import org.json.JSONObject

class LoginController(private val context: Context) {
  operator fun invoke(username: String, password: String) = Post(
    context,
    "Login",
    JSONObject()
      .put("username", username)
      .put("password", password),
    true
  )
}