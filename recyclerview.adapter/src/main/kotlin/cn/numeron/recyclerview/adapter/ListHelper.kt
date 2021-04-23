package cn.numeron.recyclerview.adapter

import cn.numeron.common.Identifiable

interface ListHelper<out T : Identifiable<*>> {

    fun getItem(position: Int): T

    fun getOrNull(position: Int): T?

    companion object {

        operator fun <T : Identifiable<*>> invoke(list: () -> List<T>): ListHelper<T> {
            return object : ListHelper<T> {
                override fun getItem(position: Int): T = list()[position]
                override fun getOrNull(position: Int): T? = list().getOrNull(position)
            }
        }

        operator fun <T : Identifiable<*>> invoke(get: (Int) -> T?): ListHelper<T> {
            return object : ListHelper<T> {
                override fun getItem(position: Int): T = get(position)!!
                override fun getOrNull(position: Int): T? = get(position)
            }
        }

    }

}