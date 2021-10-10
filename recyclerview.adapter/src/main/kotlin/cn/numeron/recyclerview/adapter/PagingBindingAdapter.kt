package cn.numeron.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import cn.numeron.common.Identifiable
import cn.numeron.stateless.livedata.StatelessLiveData
import kotlinx.coroutines.Dispatchers
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
abstract class PagingBindingAdapter<T : Identifiable<*>, out VB : ViewBinding, VH : ViewBindingHolder<T, VB>>(
        private val factory: ((VB) -> VH)? = null,
        diffCallback: DiffUtil.ItemCallback<T> = IdentifiableDifferenceCallback()
) : PagingDataAdapter<T, VH>(diffCallback, workerDispatcher = Dispatchers.IO) {

    protected val listHelper = ListHelper(::getItem)

    private val inflateMethod by lazy(::getViewBindingInflateMethod)

    val clickEvent = StatelessLiveData<ClickEvent<T>>()

    val clickItemEvent = clickEvent.map(ClickEvent<T>::item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return if (factory != null) {
            val inflater = LayoutInflater.from(parent.context)
            val viewBinding = inflateMethod.invoke(null, inflater, parent, false) as VB
            factory.invoke(viewBinding).also(::attachTo)
        } else {
            throw NotImplementedError("Can'nt create VH instance, please implementation onCreateViewHolder function.")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding(position)
    }

    private fun getViewBindingInflateMethod(): Method {
        val parameterizedType = javaClass.genericSuperclass as ParameterizedType
        val viewBindingClass = parameterizedType.actualTypeArguments[1] as Class<*>
        return viewBindingClass.getMethod("inflate",
                LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
    }

    private fun attachTo(holder: VH) {
        holder.attach(listHelper) {
            clickEvent.value = ClickEvent(it, listHelper.getItem(holder.bindingAdapterPosition))
        }
    }

}