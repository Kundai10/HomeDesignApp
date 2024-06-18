package com.example.homestyler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ImageDetailsActivity : AppCompatActivity() {

    private lateinit var beforeImage: ImageView
    private lateinit var afterImage: ImageView
    private lateinit var progressbar: ProgressBar
    private lateinit var loadingTextView : TextView
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "ImageDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_details)

        beforeImage = findViewById(R.id.beforeImageView)
        afterImage = findViewById(R.id.afterImageView)
        progressbar = findViewById(R.id.progressBar4)
        loadingTextView = findViewById(R.id.loadingTextView)

        // Assuming you have a userActivityId to fetch images for
        val userActivityId = intent.getStringExtra("userActivityId")
        fetchImagesForUserActivityId(userActivityId)
    }

    private fun fetchImagesForUserActivityId(userActivityId: String?) {
        if (userActivityId != null) {
            progressbar.visibility = View.VISIBLE
            loadingTextView.visibility = View.VISIBLE
            firestore.collection("UserActivityModel")
                .whereEqualTo("userActivityId", userActivityId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val beforeImageUrl = document.getString("beforeImageUrl")
                        val afterImageUrl = document.getString("afterImageUrl")

                        Glide.with(this)
                            .load(beforeImageUrl)
                            .into(beforeImage)

                        Glide.with(this)
                            .load(afterImageUrl)
                            .into(afterImage)
                    }
                    progressbar.visibility = View.GONE
                    loadingTextView.visibility = View.GONE
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

}
