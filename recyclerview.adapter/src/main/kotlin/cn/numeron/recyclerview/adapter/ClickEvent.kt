package cn.numeron.recyclerview.adapter

import android.view.View
import cn.numeron.common.Identifiable

data class ClickEvent<T : Identifiable<*>>(val view: View, val item: T)