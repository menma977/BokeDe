package com.bokede.view.place.bet

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.CompletableJob
import org.json.JSONObject
import java.math.BigDecimal
import kotlin.math.pow

class BetActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var jobBot: CompletableJob
  private lateinit var balance: TextView
  private lateinit var removeChance: ImageButton
  private lateinit var chance: EditText
  private lateinit var addChance: ImageButton
  private lateinit var removeAmount: ImageButton
  private lateinit var amount: EditText
  private lateinit var addAmount: ImageButton
  private lateinit var startLoop: Button
  private lateinit var endLoop: Button
  private lateinit var oneShot: Button
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
    amount = findViewById(R.id.editTextAmount)
    addAmount = findViewById(R.id.buttonAddAmount)
    startLoop = findViewById(R.id.buttonStart)
    oneShot = findViewById(R.id.buttonOneShot)
    endLoop = findViewById(R.id.buttonStop)
    type = findViewById(R.id.spinnerType)
    adBanner = findViewById(R.id.adViewBanner)

    adBanner.loadAd(AdRequest.Builder().build())

    amount.setText("1")
    chance.setText("50")
    val listType = arrayOf("Random", "High", "Low")
    type.adapter = ArrayAdapter(this, R.layout.spinner_item, listType)

    listView = findViewById<RecyclerView>(R.id.lists_container).apply {
      layoutManager = LinearLayoutManager(applicationContext)
      adapter = listAdapterBet
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

    oneShot.setOnClickListener {
      if (selectBotMode()) {
        botOneShot()
      }
    }

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

  private fun botOneShot() {
    loading.openDialog()
    val cookie = user.getString("cookie")
    startLoop.isEnabled = false
    oneShot.isEnabled = false
    endLoop.isEnabled = true

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
      startLoop.isEnabled = true
      oneShot.isEnabled = true
      endLoop.isEnabled = false
      loading.closeDialog()
    }, {
      val error = HandleError(it).result()
      when {
        error.getInt("code") == 408 -> {
          Toast.makeText(applicationContext, error.getString("message"), Toast.LENGTH_SHORT).show()
        }
        else -> {
          Toast.makeText(applicationContext, error.getString("message"), Toast.LENGTH_SHORT).show()
        }
      }
      startLoop.isEnabled = true
      oneShot.isEnabled = true
      endLoop.isEnabled = false
      loading.closeDialog()
    })
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
    listAdapterBet.addItem(BotState(winStatement, typeSpinner, "${chance.text}%", Coin.decimalToCoinView(payIn), Coin.decimalToCoinView(profit)), 25)
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
        return true
      }
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