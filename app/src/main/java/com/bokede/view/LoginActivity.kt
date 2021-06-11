package com.bokede.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bokede.R
import com.bokede.controller.LoginController
import com.bokede.controller.api.HandleError
import com.bokede.controller.api.HandleResult
import com.bokede.modal.Loading
import com.bokede.model.Url
import com.bokede.model.User
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class LoginActivity : AppCompatActivity() {
  private lateinit var move: Intent
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var username: EditText
  private lateinit var password: EditText
  private lateinit var signIn: Button
  private lateinit var signUpDice: Button
  private lateinit var signUp: Button
  private lateinit var adBanner: AdView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    user = User(this)
    loading = Loading(this)

    username = findViewById(R.id.editTextTextUsername)
    password = findViewById(R.id.editTextTextPassword)
    signIn = findViewById(R.id.buttonSignIn)
    signUpDice = findViewById(R.id.buttonRegisterDice)
    signUp = findViewById(R.id.buttonSignUp)
    adBanner = findViewById(R.id.adViewBanner)

    adBanner.loadAd(AdRequest.Builder().build())

    signIn.setOnClickListener {
      login(username, password)
    }

    signUp.setOnClickListener {
      move = Intent(this, RegisterActivity::class.java)
      startActivity(move)
    }

    signUpDice.setOnClickListener {
      move = Intent(Intent.ACTION_VIEW, Uri.parse(Url.registrationLink()))
      startActivity(move)
    }
  }

  private fun doRequestPermission() {
    requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
  }

  private fun login(username: EditText, password: EditText) {
    when {
      PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
        doRequestPermission()
      }
      username.text.isEmpty() -> {
        Toast.makeText(this, "username required", Toast.LENGTH_SHORT).show()
        username.requestFocus()
      }
      password.text.isEmpty() -> {
        Toast.makeText(this, "password required", Toast.LENGTH_SHORT).show()
        password.requestFocus()
      }
      else -> {
        loading.openDialog()
        LoginController(this).invoke(username.text.toString(), password.text.toString()).cll({
          val response = HandleResult(it).result()
          if (response.getInt("code") < 400) {
            user.setString("cookie", response.getJSONObject("data").getString("SessionCookie"))
            user.setString("balance", response.getJSONObject("data").getString("Balance"))
            user.setString("betCount", response.getJSONObject("data").getString("BetCount"))
            user.setString("betWinCount", response.getJSONObject("data").getString("BetWinCount"))
            user.setString("totalPayIn", response.getJSONObject("data").getString("BetPayIn"))
            user.setString("totalPayOut", response.getJSONObject("data").getString("BetPayOut"))
            move = Intent(this, NavigationActivity::class.java)
            loading.closeParent(move)
          } else {
            Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }, {
          val response = HandleError(it).result()
          Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        })
      }
    }
  }
}