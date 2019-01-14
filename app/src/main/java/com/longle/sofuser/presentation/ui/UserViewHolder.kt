package com.longle.sofuser.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.longle.sofuser.GlideRequests
import com.longle.sofuser.R
import com.longle.sofuser.presentation.vo.User


/**
 * A RecyclerView ViewHolder that displays a user.
 */
class UserViewHolder(view: View, private val glide: GlideRequests, private val bookmarkMethod: (Boolean, User?) -> Unit) :
    RecyclerView.ViewHolder(view) {
    private val display_name: TextView = view.findViewById(R.id.display_name)
    private val reputation: TextView = view.findViewById(R.id.reputation)
    private val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
    private val bookmarked: Switch = view.findViewById(R.id.toggleBookmarked)
    private var user: User? = null

    init {
        bookmarked.setOnCheckedChangeListener { _, isChecked ->
            bookmarkMethod.invoke(isChecked, user)
        }
    }

    fun bind(user: User?) {
        this.user = user
        display_name.text = user?.display_name ?: "loading"
        reputation.text = "Reputation:" + (user?.reputation?.toString() ?: "unknown")
        bookmarked.isChecked = user?.bookmarked ?: false
        if (user?.profile_image?.startsWith("http") == true) {
            thumbnail.visibility = View.VISIBLE
            glide.load(user.profile_image)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_black_48dp)
                .into(thumbnail)
        } else {
            thumbnail.visibility = View.GONE
            glide.clear(thumbnail)
        }
    }

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, bookmarkMethod: (Boolean, User?) -> Unit): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_post_item, parent, false)
            return UserViewHolder(view, glide, bookmarkMethod)
        }
    }

    fun updateScore(item: User?) {
        user = item
    }
}