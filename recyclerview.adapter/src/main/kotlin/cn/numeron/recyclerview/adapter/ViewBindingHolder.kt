package cn.numeron.recyclerview.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable

abstract class ViewBindingHolder<T : Identifiable<*>, out B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {

    protected lateinit var list: ListHelper<T>

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
