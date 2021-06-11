package com.bokede.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bokede.R
import com.bokede.model.BotState

class BotStateAdapter(private val context: Context) : RecyclerView.Adapter<BotStateAdapter.MyViewHolder>() {

  private val myDataset = ArrayList<BotState>()

  init {
    myDataset.add(0, BotState("default", "Type", "Chance", "Amount", "Profit"))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.bot_state, parent, false)
    return MyViewHolder(layout)
  }

  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    if (myDataset[position].color == "out" && position != 0) {
      holder.action.text = myDataset[position].type
      holder.action.setTextColor(ContextCompat.getColor(context, R.color.button_danger))
      holder.chance.text = myDataset[position].chance
      holder.chance.setTextColor(ContextCompat.getColor(context, R.color.button_danger))
      holder.amount.text = myDataset[position].amount
      holder.amount.setTextColor(ContextCompat.getColor(context, R.color.button_danger))
      holder.profit.text = myDataset[position].profit
      holder.profit.setTextColor(ContextCompat.getColor(context, R.color.button_danger))
    } else if (myDataset[position].color == "in" && position != 0) {
      holder.action.text = myDataset[position].type
      holder.action.setTextColor(ContextCompat.getColor(context, R.color.button_success))
      holder.chance.text = myDataset[position].chance
      holder.chance.setTextColor(ContextCompat.getColor(context, R.color.button_success))
      holder.amount.text = myDataset[position].amount
      holder.amount.setTextColor(ContextCompat.getColor(context, R.color.button_success))
      holder.profit.text = "+${myDataset[position].profit}"
      holder.profit.setTextColor(ContextCompat.getColor(context, R.color.button_success))
    } else {
      holder.action.text = myDataset[position].type
      holder.action.setTextColor(ContextCompat.getColor(context, R.color.button_primary))
      holder.chance.text = myDataset[position].chance
      holder.chance.setTextColor(ContextCompat.getColor(context, R.color.button_primary))
      holder.amount.text = myDataset[position].amount
      holder.amount.setTextColor(ContextCompat.getColor(context, R.color.button_primary))
      holder.profit.text = myDataset[position].profit
      holder.profit.setTextColor(ContextCompat.getColor(context, R.color.button_primary))
    }
  }

  override fun getItemCount() = myDataset.size

  fun addItem(item: BotState, size: Int) {
    myDataset.add(1, item)
    if (myDataset.size > size) {
      myDataset.removeAt(size - 1)
    }
    this.notifyDataSetChanged()
    this.notifyItemRangeInserted(0, myDataset.size)
  }

  fun clear() {
    myDataset.clear()
    myDataset.add(0, BotState("default", "Type", "Chance", "Amount", "Profit"))
    this.notifyDataSetChanged()
    this.notifyItemRangeInserted(0, myDataset.size)
  }


  class MyViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
    val action: TextView = layout.findViewById(R.id.action)
    val chance: TextView = layout.findViewById(R.id.chance)
    val amount: TextView = layout.findViewById(R.id.amount)
    val profit: TextView = layout.findViewById(R.id.profit)
  }
}