package cn.numeron.recyclerview.adapter

import androidx.lifecycle.*

open class Observable<T>(value: T? = null) {

    var value: T? = value
        @Synchronized
        set(value) {
            field = value
            notifyObservers(value)
        }

    private val observers = LinkedHashSet<Observer<T>>()

    private fun notifyObservers(value: T?) {
        for (observer in observers) {
            observer.onChanged(value)
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observers.add(observer)
        if (observers.size == 1) {
            //当添加了第一个观察者的时候，调用[onActive]
            onActive()
        }
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy(source: LifecycleOwner) {
                source.lifecycle.removeObserver(this)
                observers.remove(observer)
                if (observers.isEmpty()) {
                    //当所有的观察者都被移除掉的时候，调用[onInactive]
                    onInactive()
                }
            }
        })
    }

    protected open fun onActive() = Unit

    protected open fun onInactive() = Unit

    fun <R> map(transform: (T) -> R): Observable<R> {
        return object : Observable<R>(), Observer<T> {
            override fun onInactive() {
                observers.remove(this)
            }

            override fun onActive() {
                observers.add(this)
            }

            override fun onChanged(tValue: T) {
                value = transform(tValue)
            }
        }
    }

}