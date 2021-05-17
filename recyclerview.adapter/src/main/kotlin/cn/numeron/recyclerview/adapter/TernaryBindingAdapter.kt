package cn.numeron.recyclerview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable

/** 由三部分组成的绑定适配器， */
@Suppress("UNCHECKED_CAST")
abstract class TernaryBindingAdapter<T : Identifiable<*>> : RecyclerView.Adapter<ViewBindingHolder<out Identifiable<*>, ViewBinding>>() {

    @Suppress("LeakingThis")
    private val _differ = TernaryAsyncDiffer<T>(this, IdentifiableDifferenceCallback())

    internal val differ: AsyncListDiffer<T>
        get() = _differ

    var headerCount by _differ::headerCount

    var trailCount by _differ::trailCount

    var list: List<T>
        get() = ArrayList(differ.currentList.toList())
        set(value) = submitList(value)

    init {
        if (list.isNotEmpty()) {
            this.list = list
        }
    }

    final override fun getItemViewType(position: Int): Int {
        return when {
            position < headerCount -> R.id.occupied_item_view_header
            position < headerCount + _differ.currentList.size -> R.id.occupied_item_view_body
            position < headerCount + _differ.currentList.size + trailCount -> R.id.occupied_item_view_trails
            else -> throw IllegalStateException("Cannot determine viewType at position: $position.")
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        return when (viewType) {
            R.id.occupied_item_view_header -> onCreateHeaderViewHolder(parent)
            R.id.occupied_item_view_body -> onCreateBodyViewHolder(parent)
            R.id.occupied_item_view_trails -> onCreateTrailViewHolder(parent)
            else -> throw IllegalStateException("Unknown viewType: $viewType.")
        }
    }

    final override fun onBindViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        when (val viewType = getItemViewType(position)) {
            R.id.occupied_item_view_header -> onBindHeaderViewHolder(holder, position)
            R.id.occupied_item_view_body -> onBindBodyViewHolder(holder, position - headerCount)
            R.id.occupied_item_view_trails -> onBindTrailViewHolder(holder, position - headerCount - _differ.currentList.size)
            else -> throw IllegalStateException("Unknown viewType: $viewType.")
        }
    }

    final override fun getItemCount(): Int = headerCount + _differ.currentList.size + trailCount

    open fun onCreateHeaderViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        throw NotImplementedError("Please override onCreateHeaderViewHolder function.")
    }

    open fun onCreateBodyViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        throw NotImplementedError("Please override onCreateBodyViewHolder function.")
    }

    open fun onCreateTrailViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        throw NotImplementedError("Please override onCreateTrailViewHolder function.")
    }

    protected open fun onBindHeaderViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        holder.binding(position)
    }

    protected open fun onBindBodyViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        holder.binding(position)
    }

    protected open fun onBindTrailViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        holder.binding(position)
    }

    /** 更新适配器中的数据列表 */
    open fun submitList(list: List<T>) {
        _differ.submitList(list)
    }

    fun addListChangedListener(l: AsyncListDiffer.ListListener<T>) {
        _differ.addListListener(l)
    }

    fun removeListChangedListener(l: AsyncListDiffer.ListListener<T>) {
        _differ.removeListListener(l)
    }

}