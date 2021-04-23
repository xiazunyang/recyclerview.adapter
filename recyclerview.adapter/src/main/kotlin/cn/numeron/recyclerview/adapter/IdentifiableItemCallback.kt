package cn.numeron.recyclerview.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import cn.numeron.common.Identifiable

/** 当使用PagingDataAdapter时，可在Item上实现[Identifiable]接口后，可直接使用此比较工具。 */
open class IdentifiableItemCallback<T : Identifiable<*>>(vararg val positions: Int) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.identity == newItem.identity
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

}