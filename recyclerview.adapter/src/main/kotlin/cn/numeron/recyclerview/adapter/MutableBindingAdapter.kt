package cn.numeron.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable
import cn.numeron.stateless.livedata.StatelessLiveData
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
abstract class MutableBindingAdapter<T : Identifiable<*>, out VB : ViewBinding, VH : ViewBindingHolder<T, VB>>(
        private val factory: ((VB) -> VH)? = null,
        list: List<T> = emptyList(),
        itemDiffCallback: DiffUtil.ItemCallback<T> = IdentifiableDifferenceCallback()
) : RecyclerView.Adapter<VH>() {

    constructor(list: List<T>) : this(null, list)

    @Suppress("LeakingThis")
    protected val differ = AsyncListDiffer(this, itemDiffCallback)

    protected val inflateMethod by lazy(::getViewBindingInflateMethod)

    protected val listHelper = ListHelper(differ::getCurrentList)

    var list: List<T>
        get() = ArrayList(differ.currentList)
        set(value) = submitList(value)

    private val _clickEvent = StatelessLiveData<ClickEvent<T>>()

    val clickEvent: LiveData<ClickEvent<T>>
        get() = _clickEvent

    val clickItemEvent = _clickEvent.map(ClickEvent<T>::item)

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

    override fun getItemCount(): Int = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (factory != null) {
            val inflater = LayoutInflater.from(parent.context)
            val viewBinding = inflateMethod.invoke(null, inflater, parent, false) as VB
            return factory.invoke(viewBinding).also(::attachTo)
        } else {
            throw NotImplementedError("Can'nt create VH instance, please implementation `onCreateViewHolder` or `onCreateBodyViewHolder` function.")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
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
            _clickEvent.value = ClickEvent(it, list[holder.bindingAdapterPosition])
        }
    }

    private companion object {

        private val INFLATE_METHOD_ARGUMENTS = arrayOf(LayoutInflater::class.java,
                ViewGroup::class.java, Boolean::class.javaPrimitiveType)

    }

}