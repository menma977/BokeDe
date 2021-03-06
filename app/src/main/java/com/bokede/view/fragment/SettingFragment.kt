package com.bokede.view.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bokede.MainActivity
import com.bokede.R
import com.bokede.modal.Loading
import com.bokede.model.User
import com.bokede.tool.Coin
import com.bokede.view.NavigationActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class SettingFragment : Fragment() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var parentActivity: NavigationActivity
  private lateinit var balanceAmount: TextView
  private lateinit var signOut: Button
  private lateinit var adBanner: AdView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_setting, container, false)

    parentActivity = activity as NavigationActivity
    user = User(parentActivity)
    loading = Loading(parentActivity)

    balanceAmount = view.findViewById(R.id.textViewBalanceAmount)
    signOut = view.findViewById(R.id.buttonSignOut)
    adBanner = view.findViewById(R.id.adViewBanner)

    adBanner.loadAd(AdRequest.Builder().build())

    signOut.setOnClickListener {
      user.clear()
      val move = Intent(parentActivity, MainActivity::class.java)
      loading.closeParent(move)
    }

    return view
  }

  override fun onResume() {
    super.onResume()
    runBroadcast()
  }

  override fun onPause() {
    stopBroadcast()
    super.onPause()
  }

  private fun runBroadcast() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastBalance, IntentFilter("doge.balance"))
  }

  private fun stopBroadcast() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).unregisterReceiver(broadcastBalance)
  }

  private var broadcastBalance: BroadcastReceiver = object : BroadcastReceiver() {
    @SuppressLint("SetTextI18n")
    override fun onReceive(context: Context, intent: Intent) {
      try {
        balanceAmount.text = Coin.decimalToCoinView(intent.getStringExtra("balance").toString().toBigDecimal())
      } catch (e: Exception) {
        Log.e("error get service balance home fragment", e.message.toString())
      }
    }
  }
}