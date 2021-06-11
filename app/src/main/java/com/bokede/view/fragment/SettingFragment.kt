package com.bokede.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bokede.R
import com.bokede.view.NavigationActivity

class SettingFragment : Fragment() {
  private lateinit var parentActivity: NavigationActivity

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_setting, container, false)
    parentActivity = activity as NavigationActivity
    return view
  }
}