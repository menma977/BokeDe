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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bokede.R
import com.bokede.tool.Coin
import com.bokede.view.NavigationActivity

class HomeFragment : Fragment() {
  private lateinit var parentActivity: NavigationActivity
  private lateinit var profitAmount: TextView
  private lateinit var winChanceAmount: TextView
  private lateinit var balanceAmount: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_home, container, false)
    parentActivity = activity as NavigationActivity

    profitAmount = view.findViewById(R.id.textViewProfitAmount)
    winChanceAmount = view.findViewById(R.id.textViewWinChanceAmount)
    balanceAmount = view.findViewById(R.id.textViewBalanceAmount)

    return view
  }

  override fun onResume() {
    super.onResume()
    runBroadcast()
  }

  override fun onPause() {
    super.onPause()
    stopBroadcast()
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
        val payOut = intent.getStringExtra("payOut").toString().toBigDecimal()
        val payIn = intent.getStringExtra("payIn").toString().toBigDecimal()
        val profit = payOut + payIn
        val bet = intent.getStringExtra("bet").toString().toDouble()
        val win = intent.getStringExtra("win").toString().toDouble()
        val chance = (win / bet) * 100
        profitAmount.text = Coin.decimalToCoinView(profit)
        winChanceAmount.text = Coin.chanceToPercent(chance.toBigDecimal()) + "%"
        balanceAmount.text = Coin.decimalToCoinView(intent.getStringExtra("balance").toString().toBigDecimal())
      } catch (e: Exception) {
        Log.e("error get service balance home fragment", e.message.toString())
      }
    }
  }
}