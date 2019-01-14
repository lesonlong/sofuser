package com.longle.sofuser.presentation.ui

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.longle.sofuser.GlideRequests
import com.longle.sofuser.R
import com.longle.sofuser.presentation.repository.NetworkState
import com.longle.sofuser.presentation.vo.User

/**
 * A simple adapter implementation that shows Users.
 */
class UserAdapter(
    private val glide: GlideRequests,
    private val retryCallback: () -> Unit,
    private val bookmarkMethod: (Boolean, User?) -> Unit)
    : PagedListAdapter<User, RecyclerView.ViewHolder>(POST_COMPARATOR) {
    private var networkState: NetworkState? = null
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.user_post_item -> (holder as UserViewHolder).bind(getItem(position))
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(
                    networkState)
        }
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            (holder as UserViewHolder).updateScore(item)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.user_post_item -> UserViewHolder.create(parent, glide, bookmarkMethod)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.user_post_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
//        private val PAYLOAD_SCORE = Any()
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                    oldItem.display_name == newItem.display_name
//
//            override fun getChangePayload(oldItem: User, newItem: User): Any? {
//                return if (sameExceptScore(oldItem, newItem)) {
//                    PAYLOAD_SCORE
//                } else {
//                    null
//                }
//            }
        }

//        private fun sameExceptScore(oldItem: User, newItem: User): Boolean {
//            // DON'T do this copy in a real app, it is just convenient here for the demo :)
//            // because user randomizes scores, we want to pass it as a payload to minimize
//            // UI updates between refreshes
//            return oldItem.copy(display_name = newItem.display_name) == newItem
//        }
    }
}
