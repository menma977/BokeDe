package com.bokede.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bokede.R
import com.bokede.controller.BalanceController
import com.bokede.controller.WithdrawController
import com.bokede.controller.api.HandleError
import com.bokede.controller.api.HandleResult
import com.bokede.modal.Loading
import com.bokede.model.User
import com.bokede.tool.Coin
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class WithdrawActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var frameScanner: FrameLayout
  private lateinit var scannerEngine: ZXingScannerView
  private lateinit var wallet: String
  private lateinit var balance: TextView
  private lateinit var walletText: EditText
  private lateinit var amountText: EditText
  private lateinit var send: Button
  private var isHasCode = false
  private var isStart = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_withdraw)

    user = User(this)
    loading = Loading(this)
    loading.openDialog()

    balance = findViewById(R.id.textViewBalance)
    walletText = findViewById(R.id.editTextWallet)
    frameScanner = findViewById(R.id.frameLayoutScanner)
    amountText = findViewById(R.id.editTextAmount)
    send = findViewById(R.id.buttonSend)

    initScannerView()

    frameScanner.setOnClickListener {
      if (isStart) {
        scannerEngine.startCamera()
        isStart = false
      }
    }

    send.setOnClickListener {
      sendBalance()
    }

    BalanceController(this).invoke(user.getString("cookie")).cll({
      val response = HandleResult(it).result()
      if (response.getInt("code") < 400) {
        balance.text = Coin.decimalToCoinView(response.getJSONObject("data").getString("Balance").toBigDecimal())
      } else {
        Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
      }
      loading.closeDialog()
    }, {
      val response = HandleError(it).result()
      Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
      loading.closeDialog()
    })
  }

  private fun sendBalance() {
    loading.openDialog()
    WithdrawController(this).invoke(user.getString("cookie"), Coin.coinToDecimal(amountText.text.toString().toBigDecimal()).toPlainString(), walletText.text.toString()).cll({
      val response = HandleResult(it).result()
      if (response.getInt("code") < 400) {
        Toast.makeText(applicationContext, "Transfer pending. you can check in 999doge.com", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
      }
      loading.closeDialog()
    }, {
      val response = HandleError(it).result()
      Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
      loading.closeDialog()
    })
  }

  override fun handleResult(rawResult: Result?) {
    if (rawResult?.text?.isNotEmpty()!!) {
      isHasCode = true
      wallet = rawResult.text.toString()
      walletText.setText(wallet)
    } else {
      isHasCode = false
    }
  }

  private fun initScannerView() {
    scannerEngine = ZXingScannerView(this)
    scannerEngine.setAutoFocus(true)
    scannerEngine.setResultHandler(this)
    frameScanner.addView(scannerEngine)
  }

  override fun onPause() {
    scannerEngine.stopCamera()
    super.onPause()
  }
}