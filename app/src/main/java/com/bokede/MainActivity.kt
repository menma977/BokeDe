package com.bokede

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bokede.model.User
import com.bokede.view.LoginActivity
import com.bokede.view.NavigationActivity

class MainActivity : AppCompatActivity() {
  private lateinit var move: Intent
  private lateinit var user: User

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    user = User(this)
    if (user.getString("cookie").isNotEmpty()) {
      move = Intent(this, NavigationActivity::class.java)
      startActivity(move)
      finishAffinity()
    } else {
      move = Intent(this, LoginActivity::class.java)
      startActivity(move)
      finishAffinity()
    }
  }
}