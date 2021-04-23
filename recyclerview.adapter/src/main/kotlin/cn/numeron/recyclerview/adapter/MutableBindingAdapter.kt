package cn.numeron.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
abstract class MutableBindingAdapter<T : Identifiable<*>, out VB : ViewBinding, VH : ViewBindingHolder<T, VB>>(
        private val factory: ((VB) -> VH)? = null,
        list: List<T> = emptyList(),
        itemDiffCallback: DiffUtil.ItemCallback<T> = IdentifiableItemCallback()
) : RecyclerView.Adapter<VH>() {

    constructor(list: List<T>) : this(null, list)

    @Suppress("LeakingThis")
    private val _differ = PlaceholderListDiffer(this, itemDiffCallback)

    protected val itemInflateMethod by lazy(::getViewBindingInflateMethod)

    protected val listHelper = ListHelper(_differ::getItem)

    protected val differ: AsyncListDiffer<T>
        get() = _differ

    var placeholderCount by _differ::placeholderCount

    var list: List<T>
        get() = differ.currentList
        set(value) = submitList(value)

    val clickEvent = Observable<ClickEvent<T>>()

    val clickItemEvent = clickEvent.map(ClickEvent<T>::item)

    init {
        if (list.isNotEmpty()) {
            this.list = list
        }
    }

    private fun getViewBindingInflateMethod(): Method {
        val parameterizedType = javaClass.genericSuperclass as ParameterizedType
        val viewBindingClass = parameterizedType.actualTypeArguments[1] as Class<*>
        return viewBindingClass.getMethod("inflate", *INFLATE_METHOD_ARGUMENTS)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return if (factory != null) {
            val inflater = LayoutInflater.from(parent.context)
            val viewBinding = itemInflateMethod.invoke(null, inflater, parent, false) as VB
            factory.invoke(viewBinding).also(::attachTo)
        } else {
            throw NotImplementedError("Can'nt create VH instance, please implementation onCreateViewHolder function.")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding(position)
    }

    override fun getItemCount(): Int = _differ.itemCount

    /** 更新适配器中的数据列表 */
    open fun submitList(list: List<T>) {
        differ.submitList(list)
    }

    fun addListChangedListener(l: AsyncListDiffer.ListListener<T>) {
        differ.addListListener(l)
    }

    fun removeListChangedListener(l: AsyncListDiffer.ListListener<T>) {
        differ.removeListListener(l)
    }

    protected fun attachTo(holder: VH) {
        holder.attach(listHelper) {
            clickEvent.value = ClickEvent(it, listHelper.getItem(holder.bindingAdapterPosition))
        }
    }

    private companion object {

        private val INFLATE_METHOD_ARGUMENTS = arrayOf(LayoutInflater::class.java,
                ViewGroup::class.java, Boolean::class.javaPrimitiveType)

    }

}