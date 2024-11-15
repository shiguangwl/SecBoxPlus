package com.xxhoz.secbox.base.list.multitype

import androidx.recyclerview.widget.RecyclerView

/**
 * @author Drakeet Xu
 */
abstract class ViewHolderInflater<T, VH : RecyclerView.ViewHolder> : ItemViewBinder<T, VH>()
