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
) : RecyclerView.Adapter<ViewBindingHolder<out Identifiable<*>, ViewBinding>>() {

    constructor(list: List<T>) : this(null, list)

    @Suppress("LeakingThis")
    private val _differ = PlaceholderListDiffer(this, itemDiffCallback)

    protected val itemInflateMethod by lazy(::getViewBindingInflateMethod)

    protected val listHelper = ListHelper(_differ::getItem)

    protected val differ: AsyncListDiffer<T>
        get() = _differ

    var headerCount by _differ::headerCount

    var trailCount by _differ::trailCount

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

    override fun getItemViewType(position: Int): Int {
        return when {
            position < headerCount -> ITEM_VIEW_TYPE_HEADER
            position < headerCount + list.size -> ITEM_VIEW_TYPE_BODY
            else -> ITEM_VIEW_TYPE_TRAIL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> onCreateHeaderViewHolder(parent)
            ITEM_VIEW_TYPE_BODY -> onCreateBodyViewHolder(parent).also(::attachTo)
            else -> onCreateTrailViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_HEADER -> onBindHeaderViewHolder(holder, position)
            ITEM_VIEW_TYPE_BODY -> onBindBodyViewHolder(holder as VH, position - headerCount)
            else -> onBindTrailViewHolder(holder, position - headerCount - list.size)
        }
    }

    override fun getItemCount(): Int = headerCount + list.size + trailCount

    open fun onCreateHeaderViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        throw NotImplementedError()
    }

    open fun onCreateBodyViewHolder(parent: ViewGroup): VH {
        if (factory != null) {
            val inflater = LayoutInflater.from(parent.context)
            val viewBinding = itemInflateMethod.invoke(null, inflater, parent, false) as VB
            return factory.invoke(viewBinding).also(::attachTo)
        } else {
            throw NotImplementedError("Can'nt create VH instance, please implementation `onCreateViewHolder` or `onCreateBodyViewHolder` function.")
        }
    }

    open fun onCreateTrailViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        throw NotImplementedError()
    }

    open fun onBindHeaderViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        holder.binding(position)
    }

    open fun onBindBodyViewHolder(holder: VH, position: Int) {
        holder.binding(position)
    }

    open fun onBindTrailViewHolder(holder: ViewBindingHolder<out Identifiable<*>, ViewBinding>, position: Int) {
        holder.binding(position)
    }

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
            clickEvent.value = ClickEvent(it, listHelper.getItem(holder.bindingAdapterPosition - headerCount))
        }
    }

    private companion object {

        private const val ITEM_VIEW_TYPE_HEADER = 1
        private const val ITEM_VIEW_TYPE_BODY = 2
        private const val ITEM_VIEW_TYPE_TRAIL = 3

        private val INFLATE_METHOD_ARGUMENTS = arrayOf(LayoutInflater::class.java,
                ViewGroup::class.java, Boolean::class.javaPrimitiveType)

    }

}