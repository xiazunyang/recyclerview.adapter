# RecyclerView.Adapter
# 当前最新版本号：[![](https://jitpack.io/v/cn.numeron/recyclerview.adapter.svg)](https://jitpack.io/#cn.numeron/recyclerview.adapter)

###
* 辅助android开发者快速创建RecyclerView.Adapter的实现。
* 对于已引入ViewBinding和DataBinding的项目极其友好，仅需要2-5行代码。
* 提供DataBinding、ViewBinding配合普通列表及Paging3的各种适配器，可以满足各种需求。

### 在数据类上实现Identifiable<T>接口
```kotlin
//数据类，需实现Identifiable<T>接口并实现identity的获取方式
//identity可以是任意类型，在泛型参数中指定即可
data class Item(val id: Int, val name: String): Identifiable<Int> {
    override val identity: Int 
        get() = id
}
```

### DataBinding
* 如果使用DataBinding，仅需声明一个Adapter，即可完成创建工作。
```kotlin
//继承自AutomaticBindingAdapter声明一个Adapter
//需要在ItemBinding对应的xml中声明指定类型的变量：
// <variable name="item" type="Item" />
// <variable name="onClickListener" type="View.OnClickListener" />
//并将item与onClickListener绑定到对应的View上，View.OnClickListener为可选项
class ItemAdapter : AutomaticBindingAdapter<Item, ItemBinding>()

//然后开始使用
val itemAdapter = ItemAdapter()
recyclerView.adapter = itemAdapter
//提交数据到Adapter
itemListViewModel.itemListLiveData.observe(this) { list ->
    itemAdapter.submitList(list)    
}
//监听点击事件
itemAdapter.clickEvent.observe(this) { (view: View, item: Item) ->
    //do something...
}
//或只关心数据类的点击事件
itemAdapter.clickItemEvent.observe(this) { item: Item ->
    //do something...
}
```

### ViewBinding
* 当AutomaticBindingAdapter无法满足需求时，可使用MutableBindingAdapter + ViewBindingHolder
```kotlin
//创建Adapter，继承自MutableBindingAdapter，共3个泛型参数，分别是：
// T  -> 实现Identifiable接口的数据类
// VH -> ViewBindingHolder的实现类
// VB -> 根据布局自动生成的ViewBinding或ViewDataBinding实现类
//需要向构造方法中传递一个从VH创建VB的工厂方法，一般直接引用VH实现类的构造方法即可
//如果没有传递这个参数，则需要实现onCreateViewHolder方法
class ItemAdapter : MutableBindingAdapter<Item, ItemBinding, ItemHolder>(::ItemHolder)

//声明复用的ViewHolder，有两个泛型参数，分别是：
// T  -> 实现Identifiable接口的数据类
// VB -> Item布局自动生成的ViewBinding实现类
// 实现binding方法，可满足各种需求。
class ItemHolder(binding: ItemBinding) : ViewBindingHolder<Item, ItemBinding>(binding) {

    init {
        binding.root.setOnClickListener {
            val item = list.getItem(layoutPosition)
            //do something...
        }
    }

    override fun binding(position: Int) {
        //默认提供clickEvent点击事件，设置后，可通过监听clickEvent获取点击事件：
        //binding.root.setOnClickListener(clickEvent)
        binding.textView.text = list.getItem(position).toString()
    }
}

//使用
val itemAdapter = ItemAdapter()
recyclerView.adapter = itemAdapter
//提交数据到Adapter
itemListViewModel.itemListLiveData.observe(this) { list ->
    itemAdapter.submitList(list)    
}
```

### List表头表尾占位
* 偶尔会遇到需要在RecyclerView中开始或结束的位置上显示一些固定的内容的需求，此时Adapter的getItemCount与List的size并不相等，当列表的数据发生变化时，会出现RecyclerView的动画不正常，加载不正确等问题，这里也可以提供解决方案：
```kotlin
//MutableBindingAdapter、AutomaticBindingAdapter：
class ItemAdapter : AutomaticBindingAdapter<NumberItem, NumberViewBinding>() {

    init {
        //需要显示多少个表头项和表尾项
        headerCount = 2
        trailCount = 3
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        //创建表头项的ViewHolder
        return HeaderHolder(HeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onCreateTrailViewHolder(parent: ViewGroup): ViewBindingHolder<out Identifiable<*>, ViewBinding> {
        //创建表尾项的ViewHolder
        return TrailHolder(TrailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

}

class HeaderHolder(binding: HeaderViewBinding): ViewBindingHolder<out Identifiable<*>, HeaderViewBinding>(binding) {
    override fun binding(position: Int) {
        //position已校正为此ViewHolder在表头项中的真实位置
        //无法使用list: ListHelper工具
        //进行视图与数据的绑定操作
    }
}

class TrailHolder(binding: TrailViewBinding): ViewBindingHolder<out Identifiable<*>, TrailViewBinding>(binding) {
    override fun binding(position: Int) {
        //position已校正为此ViewHolder在表尾项中的真实位置
        //无法使用list: ListHelper工具
        //进行视图与数据的绑定操作
    }
}

//使用
val itemAdapter = ItemAdapter()
recyclerView.adapter = itemAdapter
//提交数据到Adapter
itemAdapter.submitList(list)    
```

![image](https://raw.githubusercontent.com/xiazunyang/recyclerview.adapter/main/preview.gif)

### paging3 + DataBinding
* 当使用Paging3与DataBinding时，也可以使用自动绑定功能：
```kotlin
//仅需把AutomaticBindingAdapter换成PagingAutomaticBindingAdapter即可。
//其它与AutomaticBindingAdapter完全一致。
class ItemAdapter : PagingAutomaticBindingAdapter<Item, ItemBinding>()
```

### paging3 + ViewBinding
* 依旧提供便于扩展的PagingBindingAdapter
```kotlin
//仅需把MutableBindingAdapter换成PagingBindingAdapter即可。
class ItemAdapter : PagingBindingAdapter<Item, ItemBinding, ItemHolder>(::ItemHolder)
```

### 引入

1.  在你的android工程的根目录下的build.gradle文件中的适当的位置添加以下代码：
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2.  在你的android工程中对应的android模块的build.gradle文件中的适当位置添加以下代码：
```groovy
implementation 'cn.numeron:recyclerview.adapter:latest_version'
```