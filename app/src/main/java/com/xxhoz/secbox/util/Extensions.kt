/**
 * Kotlin扩展属性和扩展函数
 */
package com.xxhoz.secbox.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.xxhoz.secbox.R

/**
 * Kt扩展属性，判断Activity是否存活
 */
val Activity?.isAlive: Boolean
    get() = !(this?.isDestroyed ?: true || this?.isFinishing ?: true)

/**
 * Boolean转Visibility
 */
fun Boolean.toVisibility() = if (this) View.VISIBLE else View.GONE

/**
 * Context转Activity
 */
fun Context.getActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> {
            this
        }
        is ContextWrapper -> {
            this.baseContext.getActivity()
        }
        else -> null
    }
}

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp: Float
    get() = this.toFloat().dp

val Float.half: Float
    get() = this / 2F


val Int.half: Int
    get() = (this / 2F).toInt()

val Float.radians: Float
    get() = Math.toRadians(this.toDouble()).toFloat()

fun ImageView.setImageUrl(url: String) {
    val options: RequestOptions = RequestOptions()
        .placeholder(R.drawable.loading_mask)
        .error(R.color.bg_image)
    Glide.with(context)
        .applyDefaultRequestOptions(options)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setImageUrlNoLoading(url: String) {
    val options: RequestOptions = RequestOptions()
        .error(R.color.bg_image)
    Glide.with(context)
        .applyDefaultRequestOptions(options)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

val String?.nullSafeValue: String
    get() = this ?: ""

fun RecyclerView.ViewHolder.getResources(): Resources {
    return itemView.resources
}

fun RecyclerView.ViewHolder.getContext(): Context {
    return itemView.context
}

fun RecyclerView.ViewHolder.getActivity(): FragmentActivity {
    return getContext().getActivity()!!
}

fun <T> Result<T>.get(): T {
    return this.getOrNull()!!
}

fun Result<*>.exception(): Throwable {
    return this.exceptionOrNull()!!
}

//// 保存携程任务
//val jobsMap:HashMap<String,Job> = HashMap()
//
///**
// * 提交任务,同时只能一个任务在执行
// */
//fun LifecycleCoroutineScope.submitTask(taskName:String,jobs:suspend ()->Unit){
//    // 如果有当前任务在执行,则取消当前任务组
//    if (jobsMap.containsKey(taskName)){
//        jobsMap[taskName]?.cancel()
//    }
//    GlobalScope.launch { jobs() }.apply {
//        jobsMap[taskName] = this
//    }
//}
