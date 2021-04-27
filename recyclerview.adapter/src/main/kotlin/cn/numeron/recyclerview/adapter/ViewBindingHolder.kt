package cn.numeron.recyclerview.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable

abstract class ViewBindingHolder<T : Identifiable<*>, out B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {

    /** 获取Adapter中表头项的数量 */
    val headerCount: Int
        get() = (bindingAdapter as? MutableBindingAdapter<*, *, *>)?.headerCount ?: 0

    /** 获取Adapter中列表项的数量 */
    val bodyCount: Int
        get() {
            val bindingAdapter = bindingAdapter
            if (bindingAdapter is MutableBindingAdapter<*, *, *>) {
                return bindingAdapter.list.size
            }
            return bindingAdapter?.itemCount ?: 0
        }

    /** 获取Adapter中表尾项的数量 */
    val trailCount: Int
        get() = (bindingAdapter as? MutableBindingAdapter<*, *, *>)?.trailCount ?: 0

    /** 返回此ViewHolder在Adapter中作为表头项的位置 */
    val headerPosition: Int
        get() = bindingAdapterPosition

    /** 返回此ViewHolder在Adapter中作为列表项的位置 */
    val bodyPosition: Int
        get() = bindingAdapterPosition - headerCount

    /** 返回此ViewHolder在Adapter中作为表尾项的位置 */
    val trailPosition: Int
        get() = bindingAdapterPosition - headerCount - bodyCount

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
