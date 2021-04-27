package cn.numeron.recyclerview.adapter

import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.DiffUtil
import cn.numeron.common.Identifiable
import java.lang.reflect.Method
import java.util.concurrent.Executor

internal class PlaceholderListDiffer<T : Identifiable<*>>(
        listUpdateCallback: ListUpdateCallback,
        private val config: AsyncDifferConfig<T>
) : AsyncListDiffer<T>(listUpdateCallback, config) {

    constructor(adapter: RecyclerView.Adapter<*>, diffCallback: DiffUtil.ItemCallback<T>)
            : this(AdapterListUpdateCallback(adapter), AsyncDifferConfig.Builder(diffCallback).build())

    private val latchListMethod: Method
    private val mainThreadExecutor: Executor

    internal var headerCount = 0
        set(value) {
            field = value
            submitList(currentList)
        }

    internal var trailCount = 0
        set(value) {
            field = value
            submitList(currentList)
        }

    init {
        val asyncListDiffer = AsyncListDiffer::class.java
        latchListMethod = asyncListDiffer.getDeclaredMethod("latchList",
                List::class.java, DiffUtil.DiffResult::class.java, Runnable::class.java)
        latchListMethod.isAccessible = true
        val mainExecutorField = asyncListDiffer.getDeclaredField("mMainThreadExecutor")
        mainExecutorField.isAccessible = true
        mainThreadExecutor = mainExecutorField.get(this) as Executor
    }

    fun getItem(position: Int): T? = currentList.getOrNull(position)

    override fun submitList(list: MutableList<T>?, commitCallback: Runnable?) {
        if (list == null || headerCount == 0 && trailCount == 0) {
            //使用原方式计算列表差异
            super.submitList(list, commitCallback)
        } else {
            //使用占位比较器计算列表的差异
            val oldList = currentList.toList()
            config.backgroundThreadExecutor.execute {
                val diffResult = DiffUtil.calculateDiff(IdentifiableCallback(oldList, list, headerCount, trailCount))
                mainThreadExecutor.execute {
                    latchListMethod.invoke(this, list, diffResult, commitCallback)
                }
            }
        }
    }

}