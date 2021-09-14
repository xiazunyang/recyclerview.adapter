package cn.numeron.recyclerview.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import cn.numeron.common.Identifiable
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

open class AutomaticBindingHolder<T : Identifiable<*>, DB : ViewDataBinding>(binding: DB) : ViewBindingHolder<T, DB>(binding) {

    private val dataBindingClassName: String

    init {
        var dataBindingClass: Class<*>? = binding.javaClass
        while (dataBindingClass != null && !Modifier.isAbstract(dataBindingClass.modifiers)) {
            dataBindingClass = dataBindingClass.superclass
        }
        dataBindingClassName = dataBindingClass?.name
                ?: throw IllegalStateException("The implementation of the ViewDataBinding could not be found.")
        if (bindItemMethods[dataBindingClassName] == null) {
            val type = javaClass.genericSuperclass
            if (type is ParameterizedType) {
                findMethods(type)
            }
        }
    }

    override fun binding(position: Int) {
        val item = list.getOrNull(position)
        bindItemMethods[dataBindingClassName]?.invoke(binding, item)
        bindClickMethods[dataBindingClassName]?.invoke(binding, clickEvent)
        binding.executePendingBindings()
    }

    companion object {

        private val bindItemMethods = mutableMapOf<String, Method>()
        private val bindClickMethods = mutableMapOf<String, Method>()

        fun findMethods(type: ParameterizedType) {
            val actualTypeArguments = type.actualTypeArguments
            val dataBindingClass = actualTypeArguments[1] as? Class<*> ?: return
            val itemClass = actualTypeArguments[0] as? Class<*>
            val onClickClass = View.OnClickListener::class.java
            dataBindingClass.methods.forEach {
                val parameterTypes = it.parameterTypes
                val firstClass = parameterTypes.firstOrNull()
                if (parameterTypes.size == 1 && firstClass == itemClass) {
                    bindItemMethods[dataBindingClass.name] = it
                }
                if (parameterTypes.size == 1 && firstClass == onClickClass) {
                    bindClickMethods[dataBindingClass.name] = it
                }
            }
        }

    }

}