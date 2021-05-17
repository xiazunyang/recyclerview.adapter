package cn.numeron.recyclerview.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable

abstract class ViewBindingHolder<T : Identifiable<*>, out B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {

    protected lateinit var list: ListHelper<T>

    val headerCount: Int
        get() = (bindingAdapter as? TernaryBindingAdapter<*>)?.headerCount ?: 0

    val bodyCount: Int
        get() {
            val bindingAdapter = bindingAdapter
            if (bindingAdapter is TernaryBindingAdapter<*>) {
                return bindingAdapter.differ.currentList.size
            }
            return bindingAdapter?.itemCount ?: 0
        }

    val trailCount: Int
        get() = (bindingAdapter as? TernaryBindingAdapter<*>)?.trailCount ?: 0

    val localPosition: Int
        get() {
            val position = bindingAdapterPosition
            return when (bindingAdapter?.getItemViewType(position)) {
                R.id.occupied_item_view_header -> position
                R.id.occupied_item_view_body -> position - headerCount
                R.id.occupied_item_view_trails -> position - headerCount - bodyCount
                else -> throw IllegalStateException("Unknown item view type: $itemViewType.")
            }
        }

    protected var clickEvent: View.OnClickListener?
        get() = itemView.getTag(R.id.id_view_tag_click_event) as? View.OnClickListener
        set(value) {
            itemView.setTag(R.id.id_view_tag_click_event, value)
        }

    internal fun attach(listHelper: ListHelper<T>, clickEvent: View.OnClickListener) {
        this.list = listHelper
        this.clickEvent = clickEvent
    }

    abstract fun binding(position: Int)

}
