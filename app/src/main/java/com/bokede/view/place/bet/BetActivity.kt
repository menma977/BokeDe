package com.bokede.view.place.bet

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bokede.R
import com.bokede.adapter.BotStateAdapter
import com.bokede.controller.BalanceController
import com.bokede.controller.BotController
import com.bokede.controller.api.HandleError
import com.bokede.controller.api.HandleResult
import com.bokede.modal.Loading
import com.bokede.model.BotState
import com.bokede.model.User
import com.bokede.tool.Coin
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.math.BigDecimal
import kotlin.math.pow

class BetActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var job: CompletableJob
  private lateinit var balance: TextView
  private lateinit var removeChance: ImageButton
  private lateinit var chance: EditText
  private lateinit var addChance: ImageButton
  private lateinit var removeAmount: ImageButton
  private lateinit var amountDefault: EditText
  private lateinit var amount: EditText
  private lateinit var addAmount: ImageButton
  private lateinit var startLoop: Button
  private lateinit var endLoop: Button
  private lateinit var oneShot: Button
  private lateinit var doubleButton: Button
  private lateinit var halfButton: Button
  private lateinit var resetButton: Button
  private lateinit var type: Spinner
  private lateinit var balanceRaw: BigDecimal
  private lateinit var listView: RecyclerView
  private lateinit var listAdapterBet: BotStateAdapter
  private lateinit var adBanner: AdView
  private var seed: String = (100000..999999).random().toString()
  private var currentHigh = BigDecimal(49.999)
  private var currentLow = BigDecimal(0)
  private var highModeHigh = BigDecimal(99.999)
  private var highModeLow = BigDecimal(50)
  private var lowModeHigh = BigDecimal(49.999)
  private var lowModeLow = BigDecimal(0)
  private var typeSpinner = "Random"
  private var isResponse = true

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bet)

    user = User(this)
    loading = Loading(this)
    listAdapterBet = BotStateAdapter(this)

    balance = findViewById(R.id.textViewBalance)
    removeChance = findViewById(R.id.buttonRemoveChance)
    chance = findViewById(R.id.editTextChance)
    addChance = findViewById(R.id.buttonAddChance)
    removeAmount = findViewById(R.id.buttonRemoveAmount)
    amountDefault = findViewById(R.id.editTextAmountDefault)
    amount = findViewById(R.id.editTextAmount)
    addAmount = findViewById(R.id.buttonAddAmount)
    startLoop = findViewById(R.id.buttonStart)
    oneShot = findViewById(R.id.buttonOneShot)
    endLoop = findViewById(R.id.buttonStop)
    doubleButton = findViewById(R.id.buttonDouble)
    halfButton = findViewById(R.id.buttonHalf)
    resetButton = findViewById(R.id.buttonReset)
    type = findViewById(R.id.spinnerType)
    adBanner = findViewById(R.id.adViewBanner)

    adBanner.loadAd(AdRequest.Builder().build())

    amountDefault.setText("0.000001")
    amount.setText("0.000001")
    chance.setText("50")
    val listType = arrayOf("Random", "High", "Low")
    type.adapter = ArrayAdapter(this, R.layout.spinner_item, listType)

    listView = findViewById<RecyclerView>(R.id.lists_container).apply {
      layoutManager = LinearLayoutManager(applicationContext)
      adapter = listAdapterBet
    }

    amountDefault.doAfterTextChanged {
      amount.setText(it.toString())
    }

    removeChance.setOnClickListener {
      val localChance = chance.text.toString().toDouble()
      if ((localChance - 1) <= 5) {
        chance.setText("5")
      } else {
        chance.setText((localChance - 1).toString())
      }
    }

    addChance.setOnClickListener {
      val localChance = chance.text.toString().toDouble()
      if ((localChance + 1) >= 100) {
        chance.setText("100")
      } else {
        chance.setText((localChance + 1).toString())
      }
    }

    removeAmount.setOnClickListener {
      val localAmount = amount.text.toString().replace(",", ".").toDouble().toBigDecimal()
      if (localAmount > BigDecimal(1)) {
        amount.setText(Coin.decimalToCoin(Coin.coinToDecimal((localAmount.minus(BigDecimal.ONE)))).toPlainString())
      } else {
        val calculate = BigDecimal(1) / localAmount
        if (calculate <= 10.0.pow(8.0).toBigDecimal()) {
          if (!isPowerOfTen(calculate)) {
            var tmpAmount = ""
            if (localAmount.toPlainString().length <= 2) {
              tmpAmount = localAmount.toPlainString().substring(2)
            }
            var n = 0
            for (i in tmpAmount) {
              if (i != '0') {
                break
              }
              n++
            }

            val range = 10.0.pow(n + 0.0)
            val exp = if (range <= 0) 1 else range
            amount.setText(Coin.decimalToCoin(Coin.coinToDecimal((localAmount.minus(exp.toString().reversed().toBigDecimal())))).toPlainString())
          } else {
            val dec = BigDecimal(1) / (calculate * BigDecimal(10))
            amount.setText(Coin.decimalToCoin(Coin.coinToDecimal((localAmount.minus(dec)))).toPlainString())
          }
        } else {
          amount.setText(Coin.decimalToCoin(Coin.coinToDecimal((localAmount))).toPlainString())
        }
      }
    }

    addAmount.setOnClickListener {
      val localAmount = amount.text.toString().replace(",", ".").toDouble().toBigDecimal()
      if (localAmount > BigDecimal(1)) {
        amount.setText(Coin.decimalToCoin(Coin.coinToDecimal((localAmount.plus(BigDecimal.ONE)))).toPlainString())
      } else {
        var tmpAmount = ""
        if (localAmount.toPlainString().length <= 2) {
          tmpAmount = localAmount.toPlainString().substring(2)
        }
        var n = 0
        for (i in tmpAmount) {
          if (i != '0') {
            break
          }
          n++
        }

        val range = 10.0.pow(n + 0.0)
        val exp = if (range <= 0) 1 else range
        amount.setText(Coin.decimalToCoin(Coin.coinToDecimal((localAmount.plus(exp.toString().reversed().toBigDecimal())))).toPlainString())
      }
    }

    doubleButton.setOnClickListener {
      val localAmount = amount.text.toString().replace(",", ".").toBigDecimal()
      amount.setText(Coin.decimalToCoin(Coin.coinToDecimal(localAmount.multiply(BigDecimal(2)))).toPlainString())
    }

    halfButton.setOnClickListener {
      val localAmount = amount.text.toString().replace(",", ".").toBigDecimal()
      amount.setText(Coin.decimalToCoin(Coin.coinToDecimal(localAmount.divide(BigDecimal(2)))).toPlainString())
    }

    resetButton.setOnClickListener {
      amount.text = amountDefault.text
    }

    oneShot.setOnClickListener {
      if (selectBotMode()) {
        botOneShot()
      }
    }

    startLoop.setOnClickListener {
      if (selectBotMode()) {
        startLoop.isEnabled = false
        oneShot.isEnabled = false
        endLoop.isEnabled = true
        initBot()
      }
    }

    endLoop.setOnClickListener {
      job.cancel(CancellationException("Bot has been stop"))
      startLoop.isEnabled = true
      oneShot.isEnabled = true
      endLoop.isEnabled = false
    }

    listAdapterBet.clear()
    initBalance()
  }

  private fun initBalance() {
    loading.openDialog()
    BalanceController(this).invoke(user.getString("cookie")).cll({
      val response = HandleResult(it).result()
      if (response.getInt("code") < 400) {
        balanceRaw = response.getJSONObject("data").getString("Balance").toBigDecimal()
        balance.text = Coin.decimalToCoin(balanceRaw).toPlainString()
        loading.closeDialog()
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

  private fun initBot() {
    if (!::job.isInitialized || job.isCompleted) {
      job = Job()
      job.invokeOnCompletion { throwable ->
        throwable?.message.let {
          var message = it
          if (message.isNullOrBlank()) {
            message = "null error"
          }
          GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    botLoop()
  }

  private fun botOneShot() {
    val cookie = user.getString("cookie")
    oneShot.isEnabled = false
    val payIn = Coin.coinToDecimal(amount.text.toString().toBigDecimal()).toPlainString()
    BotController(this).manual(
      cookie,
      payIn,
      Coin.percentToChance(currentLow),
      Coin.percentToChance(currentHigh),
      seed
    ).cll({
      val response = HandleResult(it).result()
      when {
        response.getInt("code") < 400 -> {
          response.getJSONObject("data").put("PayIn", payIn)
          botData(response.getJSONObject("data"))
        }
        response.getInt("code") == 408 -> {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
        }
        else -> {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
        }
      }
      oneShot.isEnabled = true
    }, {
      val error = HandleError(it).result()
      Toast.makeText(applicationContext, error.getString("message"), Toast.LENGTH_SHORT).show()
      oneShot.isEnabled = true
    })
  }

  private fun botLoop() {
    val cookie = user.getString("cookie")
    CoroutineScope(Dispatchers.IO + job).launch {
      var dynamicTime = 100L
      while (isActive) {
        if (isResponse) {
          delay(dynamicTime)
          isResponse = false
          try {
            setUpMode()
            val payIn = Coin.coinToDecimal(amount.text.toString().toBigDecimal()).toPlainString()
            BotController(applicationContext).manual(
              cookie,
              payIn,
              Coin.percentToChance(currentLow),
              Coin.percentToChance(currentHigh),
              seed
            ).cll({
              val response = HandleResult(it).result()
              dynamicTime = when {
                response.getInt("code") < 400 -> {
                  response.getJSONObject("data").put("PayIn", payIn)
                  botData(response.getJSONObject("data"))
                  2000L
                }
                response.getInt("code") == 408 -> {
                  Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
                  15000L
                }
                else -> {
                  Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
                  8000L
                }
              }
              isResponse = true
            }, {
              val error = HandleError(it).result()
              dynamicTime = if (error.getInt("code") == 555) {
                Toast.makeText(applicationContext, "your connection is problematic wait 15 seconds to continue", Toast.LENGTH_SHORT).show()
                15000L
              } else {
                Toast.makeText(applicationContext, error.getString("message"), Toast.LENGTH_SHORT).show()
                8000L
              }
              isResponse = true
            })
          } catch (e: Exception) {
            Log.e("bot loop", e.message.toString())
            dynamicTime = 8000L
            isResponse = true
            println("error")
          }
        }
      }
    }
  }

  private fun botData(response: JSONObject) {
    seed = response.getString("Next")
    val payOut = response.getString("PayOut").toBigDecimal()
    val payIn = response.getString("PayIn").toBigDecimal()
    val profit = payOut - payIn
    balanceRaw = response.getString("StartingBalance").toBigDecimal() + profit
    var winStatement = "in"
    if (profit < BigDecimal(0)) {
      winStatement = "out"
    }
    listAdapterBet.addItem(BotState(winStatement, typeSpinner, "${chance.text}%", Coin.decimalToCoinView(payIn), Coin.decimalToCoinView(profit)), 10)
    balance.text = Coin.decimalToCoinView(balanceRaw)
  }

  private fun selectBotMode(): Boolean {
    when {
      amount.text.toString().isEmpty() -> {
        Toast.makeText(this, "amount required", Toast.LENGTH_SHORT).show()
        amount.requestFocus()
        return false
      }
      chance.text.toString().isEmpty() -> {
        Toast.makeText(this, "chance required", Toast.LENGTH_SHORT).show()
        chance.requestFocus()
        return false
      }
      chance.text.toString().toBigDecimal() < BigDecimal(0) || chance.text.toString().toBigDecimal() > BigDecimal(100) -> {
        Toast.makeText(this, "chance cannot be less or more than 0 and 100", Toast.LENGTH_SHORT).show()
        chance.requestFocus()
        return false
      }
      else -> {
        setUpMode()
        return true
      }
    }
  }

  private fun setUpMode() {
    val inputChance = chance.text.toString().toBigDecimal()
    val chanceLow = BigDecimal(0) + inputChance
    val chanceHigh = BigDecimal(100) - inputChance
    lowModeLow = BigDecimal(0)
    lowModeHigh = chanceLow
    highModeLow = chanceHigh
    highModeHigh = BigDecimal(99.999)
    if (type.selectedItemPosition == 0) {
      val getRandomType = (1..2).shuffled().last()
      if (getRandomType == 1) {
        typeSpinner = "High"
        currentLow = highModeLow
        currentHigh = highModeHigh
      } else {
        typeSpinner = "Low"
        currentLow = lowModeLow
        currentHigh = lowModeHigh
      }
    } else if (type.selectedItemPosition == 1) {
      typeSpinner = "High"
      currentLow = highModeLow
      currentHigh = highModeHigh
    } else {
      typeSpinner = "Low"
      currentLow = lowModeLow
      currentHigh = lowModeHigh
    }
  }

  private fun isPowerOfTen(value: BigDecimal): Boolean {
    if (value % BigDecimal(10) != BigDecimal(0L) || value == BigDecimal(0L)) {
      return false
    }
    if (value == BigDecimal(10L)) {
      return true
    }
    return isPowerOfTen(value / BigDecimal(10))
  }
}