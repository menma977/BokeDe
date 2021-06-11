package com.bokede.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bokede.R
import com.bokede.service.Balance
import com.bokede.view.fragment.HomeFragment
import com.bokede.view.fragment.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class NavigationActivity : AppCompatActivity() {
  private lateinit var bottomNavigationView: BottomNavigationView
  private lateinit var botFloatButton: FloatingActionButton
  private lateinit var job: CompletableJob
  private lateinit var balanceIntent: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    bottomNavigationView = findViewById(R.id.bottomNavigation)
    botFloatButton = findViewById(R.id.fabBot)

    config()
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount == 1) {
      stopService()

      finishAffinity()
    } else {
      bottomNavigationView.menu.getItem(0).isChecked = true
      super.onBackPressed()
    }
  }

  override fun onResume() {
    super.onResume()
    runService()
  }

  override fun onStop() {
    super.onStop()
    stopService()
  }

  override fun onPause() {
    super.onPause()
    stopService()
  }

  private fun config() {
    bottomNavigationView.background = null
    bottomNavigationView.setOnNavigationItemSelectedListener(navigationItem)

    addFragment(HomeFragment())

    runService()
  }

  private val navigationItem = BottomNavigationView.OnNavigationItemSelectedListener { item ->
    when (item.itemId) {
      R.id.nav_home -> {
        addFragment(HomeFragment())
        return@OnNavigationItemSelectedListener true
      }
      R.id.nav_setting -> {
        addFragment(SettingFragment())
        return@OnNavigationItemSelectedListener true
      }
      else -> {
        return@OnNavigationItemSelectedListener false
      }
    }
  }

  private fun addFragment(fragment: Fragment) {
    val backStateName = fragment.javaClass.simpleName
    val fragmentManager = supportFragmentManager
    val fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0)

    if (!fragmentPopped && fragmentManager.findFragmentByTag(backStateName) == null) {
      val fragmentTransaction = fragmentManager.beginTransaction()
      fragmentTransaction.replace(R.id.container, fragment, backStateName)
      fragmentTransaction.addToBackStack(backStateName)
      fragmentTransaction.commit()
    }
  }

  private fun runService() {
    if (!::job.isInitialized || job.isCompleted) {
      job = Job()
    }

    CoroutineScope(Dispatchers.IO + job).launch {
      balanceIntent = Intent(applicationContext, Balance::class.java)

      startService(balanceIntent)
    }
  }

  private fun stopService() {
    if (this::balanceIntent.isInitialized) {
      stopService(balanceIntent)
    }
  }
}