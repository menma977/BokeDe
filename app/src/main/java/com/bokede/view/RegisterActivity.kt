package com.bokede.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bokede.R
import com.bokede.controller.RegisterController
import com.bokede.controller.api.HandleError
import com.bokede.controller.api.HandleResult

class RegisterActivity : AppCompatActivity() {
  private lateinit var usernameReg: EditText
  private lateinit var passwordReg: EditText
  private lateinit var signUp: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)

    usernameReg = findViewById(R.id.editTextTextUsernameReg)
    passwordReg = findViewById(R.id.editTextTextPasswordReg)
    signUp = findViewById(R.id.buttonSignUp)

    signUp.setOnClickListener {
      register(usernameReg, passwordReg)
    }
  }

  private fun register(username: EditText, password: EditText) {
    when {
      username.text.isEmpty() -> {
        Toast.makeText(this, "username required", Toast.LENGTH_SHORT).show()
        username.requestFocus()
      }
      password.text.isEmpty() -> {
        Toast.makeText(this, "password required", Toast.LENGTH_SHORT).show()
        password.requestFocus()
      }
      else -> {
        RegisterController(this).createAccount().cll({ itCreateAccount ->
          val responseCreateAccount = HandleResult(itCreateAccount).result()
          if (responseCreateAccount.getInt("code") < 400) {
            RegisterController(this).invoke(responseCreateAccount.getJSONObject("data").getString("SessionCookie"), username.text.toString(), password.text.toString()).cll({
              Toast.makeText(applicationContext, "Account hash been create", Toast.LENGTH_SHORT).show()
            }, {
              val response = HandleError(it).result()
              Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
            })
          } else {
            Toast.makeText(applicationContext, responseCreateAccount.getString("data"), Toast.LENGTH_SHORT).show()
          }
        }, {
          val response = HandleError(it).result()
          Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
        })
      }
    }
  }
}