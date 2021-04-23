package cn.numeron.recyclerview.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cn.numeron.common.Identifiable
import kotlin.collections.List

/** 当RecyclerView的列表发生变化时，使用此工具来对比差异，并且处理动画 */
open class IdentifiableCallback<T : Identifiable<*>>(
        protected val oldList: List<T>,
        protected val newList: List<T>,
        protected val placeholderCount: Int) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size + placeholderCount

    override fun getNewListSize(): Int = newList.size + placeholderCount

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldItemPosition < placeholderCount || newItemPosition < placeholderCount) {
            oldItemPosition == newItemPosition
        } else {
            val correctedValue = placeholderCount.coerceAtLeast(0)
            val correctedOldPosition = oldItemPosition - correctedValue
            val correctedNewPosition = newItemPosition - correctedValue
            val oldItem = oldList[correctedOldPosition]
            val newItem = newList[correctedNewPosition]
            oldItem.identity == newItem.identity
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldItemPosition < placeholderCount || newItemPosition < placeholderCount) {
            oldItemPosition == newItemPosition
        } else {
            val correctedValue = placeholderCount.coerceAtLeast(0)
            val correctedOldPosition = oldItemPosition - correctedValue
            val correctedNewPosition = newItemPosition - correctedValue
            val oldItem = oldList[correctedOldPosition]
            val newItem = newList[correctedNewPosition]
            oldItem == newItem
        }
    }

    companion object {

        fun IdentifiableCallback<*>.dispatchUpdatesTo(adapter: RecyclerView.Adapter<*>) {
            DiffUtil.calculateDiff(this).dispatchUpdatesTo(adapter)
        }

        fun IdentifiableCallback<*>.dispatchUpdatesTo(recyclerView: RecyclerView) {
            dispatchUpdatesTo(recyclerView.adapter ?: return)
        }

    }

}