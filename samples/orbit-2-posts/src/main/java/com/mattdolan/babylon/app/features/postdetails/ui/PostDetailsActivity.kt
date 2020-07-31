package com.mattdolan.babylon.app.features.postdetails.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mattdolan.babylon.R
import com.mattdolan.babylon.domain.repositories.PostOverview
import kotlinx.android.synthetic.main.post_details_activity.*


class PostDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_details_activity)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    companion object {
        fun startIntent(context: Context, post: PostOverview) =
            Intent(context, PostDetailsActivity::class.java).apply {
                putExtra(POST_ID_EXTRA, post.id)
            }

        const val POST_ID_EXTRA = "POST_ID"
    }
}
