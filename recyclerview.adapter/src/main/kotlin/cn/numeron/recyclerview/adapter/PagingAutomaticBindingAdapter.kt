package cn.numeron.recyclerview.adapter

import androidx.databinding.ViewDataBinding
import cn.numeron.common.Identifiable
import java.lang.reflect.ParameterizedType

/**
 * 可自动绑定 + Paging3 的RecyclerView.Adapter
 * 通过反射工作，可自动绑定xml中声明的1个数据类及1个点击事件，如：
 * <variable name="item" type="Item" /> 注：type必需与T泛型一致
 * <variable name="onClickListener" type="View.OnClickListener" /> 注：必需是View.OnClickListener类型
 * */
abstract class PagingAutomaticBindingAdapter<T : Identifiable<*>, DB : ViewDataBinding> :
        PagingBindingAdapter<T, DB, AutomaticBindingHolder<T, DB>>(::AutomaticBindingHolder) {

    init {
        val parameterizedType = javaClass.genericSuperclass as ParameterizedType
        AutomaticBindingHolder.findMethods(parameterizedType)
    }

}