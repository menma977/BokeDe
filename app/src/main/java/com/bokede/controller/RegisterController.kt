package com.bokede.controller

import android.content.Context
import com.bokede.controller.api.Post
import org.json.JSONObject

class RegisterController(private val context: Context) {
  operator fun invoke(cookie: String, username: String, password: String) = Post(
    context,
    "Login",
    JSONObject()
      .put("s", cookie)
      .put("username", username)
      .put("password", password),
    true
  )

  fun createAccount() = Post(
    context,
    "CreateAccount",
    JSONObject(),
    true
  )
}