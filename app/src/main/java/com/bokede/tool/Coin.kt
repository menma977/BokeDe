package com.bokede.tool

import java.math.BigDecimal
import java.text.DecimalFormat

object Coin {
  private var longFormat = BigDecimal(100000000)
  private var bigDecimalFormat = BigDecimal(0.00000001)
  private val decimalFormat = DecimalFormat("#.########")
  private val percent = DecimalFormat("00.0000")
  private val percentView = DecimalFormat("00.00")

  fun coinToDecimal(value: BigDecimal): BigDecimal {
    return value.multiply(longFormat).setScale(0, BigDecimal.ROUND_HALF_DOWN)
  }

  fun decimalToCoin(value: BigDecimal): BigDecimal {
    return decimalFormat.format(value.multiply(bigDecimalFormat).setScale(8, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".").toBigDecimal()
  }

  fun decimalToCoinView(value: BigDecimal): String {
    return decimalFormat.format(value.multiply(bigDecimalFormat).setScale(8, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".") + "DOGE"
  }

  fun percentToChance(value: BigDecimal): String {
    return when {
      value >= BigDecimal(100.0) -> {
        percent.format(BigDecimal(99.9999).setScale(4, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".").replace(".", "")
      }
      value <= BigDecimal(0.0) -> {
        percent.format(BigDecimal(0.0005).setScale(4, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".").replace(".", "")
      }
      else -> {
        percent.format(value.setScale(4, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".").replace(".", "")
      }
    }
  }

  fun chanceToPercent(value: BigDecimal): String {
    return percentView.format(value.setScale(2, BigDecimal.ROUND_HALF_DOWN)).replace(",", ".")
  }
}