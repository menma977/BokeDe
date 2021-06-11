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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bokede.R
import com.bokede.controller.WalletController
import com.bokede.controller.api.HandleError
import com.bokede.controller.api.HandleResult
import com.bokede.modal.Loading
import com.bokede.modal.Wallet
import com.bokede.model.User
import com.bokede.tool.Coin
import com.bokede.view.NavigationActivity
import com.bokede.view.WithdrawActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class HomeFragment : Fragment() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var move: Intent
  private lateinit var parentActivity: NavigationActivity
  private lateinit var profitAmount: TextView
  private lateinit var winChanceAmount: TextView
  private lateinit var balanceAmount: TextView
  private lateinit var deposit: Button
  private lateinit var withdraw: Button
  private lateinit var adBanner: AdView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_home, container, false)

    parentActivity = activity as NavigationActivity
    user = User(parentActivity)
    loading = Loading(parentActivity)

    profitAmount = view.findViewById(R.id.textViewProfitAmount)
    winChanceAmount = view.findViewById(R.id.textViewWinChanceAmount)
    balanceAmount = view.findViewById(R.id.textViewBalanceAmount)
    deposit = view.findViewById(R.id.buttonDeposit)
    withdraw = view.findViewById(R.id.buttonWithdraw)
    adBanner = view.findViewById(R.id.adViewBanner)

    adBanner.loadAd(AdRequest.Builder().build())

    deposit.setOnClickListener {
      if (user.getString("wallet").isEmpty()) {
        loading.openDialog()
        WalletController(view.context).invoke(user.getString("cookie")).cll({
          val response = HandleResult(it).result()
          if (response.getInt("code") < 400) {
            user.setString("wallet", response.getJSONObject("data").getString("Address"))
            Wallet.show(view.context, user.getString("wallet"), "doge")
            loading.closeDialog()
          } else {
            Toast.makeText(view.context.applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }, {
          val response = HandleError(it).result()
          Toast.makeText(view.context.applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        })
      } else {
        Wallet.show(view.context, user.getString("wallet"), "doge")
      }
    }

    withdraw.setOnClickListener {
      move = Intent(parentActivity, WithdrawActivity::class.java)
      parentActivity.startActivity(move)
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
        val payOut = intent.getStringExtra("payOut").toString().toBigDecimal()
        val payIn = intent.getStringExtra("payIn").toString().toBigDecimal()
        val profit = payOut + payIn
        val bet = intent.getStringExtra("bet").toString().toDouble()
        val win = intent.getStringExtra("win").toString().toDouble()
        var chance = 50.0
        if (bet > 0 && win > 0) {
          chance = (win / bet) * 100
        }
        profitAmount.text = Coin.decimalToCoinView(profit)
        winChanceAmount.text = Coin.chanceToPercent(chance.toBigDecimal()) + "%"
        balanceAmount.text = Coin.decimalToCoinView(intent.getStringExtra("balance").toString().toBigDecimal())
      } catch (e: Exception) {
        Log.e("error get service balance home fragment", e.message.toString())
      }
    }
  }
}