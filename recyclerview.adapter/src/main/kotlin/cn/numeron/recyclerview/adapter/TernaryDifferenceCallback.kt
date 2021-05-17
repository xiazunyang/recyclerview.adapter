package cn.numeron.recyclerview.adapter

import androidx.recyclerview.widget.DiffUtil
import cn.numeron.common.Identifiable
import kotlin.collections.List

/** 当RecyclerView的列表发生变化时，使用此工具来对比差异，并且处理动画 */
internal class TernaryDifferenceCallback<T : Identifiable<*>>(
        protected val oldList: List<T>,
        protected val newList: List<T>,
        protected val headerCount: Int = 0,
        protected val trailCount: Int = 0,
) : DiffUtil.Callback() {

    init {
        require(headerCount > -1)
        require(trailCount > -1)
    }

    override fun getOldListSize(): Int = headerCount + oldList.size + trailCount

    override fun getNewListSize(): Int = headerCount + newList.size + trailCount

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldItemPosition < headerCount || newItemPosition < headerCount) {
            oldItemPosition == newItemPosition
        } else if (oldItemPosition >= headerCount + oldList.size || newItemPosition >= headerCount + newList.size) {
            val diff = oldList.size - newList.size
            oldItemPosition == newItemPosition + diff
        } else {
            val correctedOldPosition = oldItemPosition - headerCount
            val correctedNewPosition = newItemPosition - headerCount
            val oldItem = oldList[correctedOldPosition]
            val newItem = newList[correctedNewPosition]
            oldItem.identity == newItem.identity
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldItemPosition < headerCount || newItemPosition < headerCount) {
            oldItemPosition == newItemPosition
        } else if (oldItemPosition >= headerCount + oldList.size || newItemPosition >= headerCount + newList.size) {
            val diff = oldList.size - newList.size
            oldItemPosition == newItemPosition + diff
        } else {
            val correctedOldPosition = oldItemPosition - headerCount
            val correctedNewPosition = newItemPosition - headerCount
            val oldItem = oldList[correctedOldPosition]
            val newItem = newList[correctedNewPosition]
            oldItem == newItem
        }
    }

}