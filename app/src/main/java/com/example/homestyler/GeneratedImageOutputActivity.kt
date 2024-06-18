package com.example.homestyler

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.homestyler.model.UserActivityModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GeneratedImageOutputActivity : AppCompatActivity() {

    private lateinit var beforeImage: ImageView
    private lateinit var afterImage: ImageView
    private lateinit var loadingTV: TextView
    private lateinit var progressBar: ProgressBar

    private val snapAndDesignViewModel: SnapAndDesignViewModel by viewModels()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    // Declare global variables for image URLs
    private var beforeImageUrl: String = ""
    private var afterImageUrl: String = ""

    // Flags to track image upload status
    private var beforeImageUploaded = false
    private var afterImageUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generated_image_output)

        loadingTV = findViewById(R.id.loadingTextView2)
        progressBar = findViewById(R.id.progressBar6)

        beforeImage = findViewById(R.id.yourBeforeImage)
        afterImage = findViewById(R.id.yourAfterImage)

        val url = intent.getStringExtra("url") ?: ""
        snapAndDesignViewModel.getResult(url)

        progressBar.visibility = View.VISIBLE
        loadingTV.visibility =View.VISIBLE

        snapAndDesignViewModel.getResultResponse.observe(this) { response ->
            Glide.with(this)
                .load(response.input.image)
                .into(beforeImage)

            if (response.output.isNotEmpty()) {
                // Rename the local variable to avoid conflict
                val _afterImageUrl = if (response.output.size > 1) response.output[1] else response.output[0]
                Glide.with(this)
                    .load(_afterImageUrl)
                    .into(afterImage)

                val beforeImageUrl = response.input.image
                progressBar.visibility = View.GONE
                loadingTV.visibility =View.GONE
                uploadAndSaveImages(beforeImageUrl, _afterImageUrl)
            } else {
                Log.e("GeneratedImageOutput", "Failed to get result: ${response.error}")
                progressBar.visibility = View.GONE
                loadingTV.visibility =View.GONE
                Toast.makeText(this, "Failed to get URL result", Toast.LENGTH_SHORT).show()
                // Maybe go to the design screen again??
            }
        }
    }



    private fun uploadAndSaveImages(beforeImageUrl: String, afterImageUrl: String) {
        uploadImageToFirebaseStorage(beforeImageUrl, "beforeImage") {
            beforeImageUploaded = true
            Log.d("GeneratedImageOutput", "Before image uploaded")
            checkAndSaveImageUrls()
        }

        uploadImageToFirebaseStorage(afterImageUrl, "afterImage") {
            afterImageUploaded = true
            Log.d("GeneratedImageOutput", "After image uploaded")
            checkAndSaveImageUrls()
        }
    }

    private fun checkAndSaveImageUrls() {
        if (beforeImageUploaded && afterImageUploaded) {
            Log.d("GeneratedImageOutput", "Both images uploaded, saving URLs to Firestore")
            saveImageUrlToFirestore()
        } else {
            Log.d("GeneratedImageOutput", "Waiting for both images to upload")
        }
    }

    private fun uploadImageToFirebaseStorage(imageUrl: String, imageName: String, onComplete: () -> Unit) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val baos = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val storageRef = storageRef.child("images/$imageName.jpg")
                    val uploadTask = storageRef.putBytes(data)
                    uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            if (imageName == "beforeImage") {
                                this@GeneratedImageOutputActivity.beforeImageUrl = uri.toString()
                            } else {
                                this@GeneratedImageOutputActivity.afterImageUrl = uri.toString()
                            }
                            onComplete()
                        }
                    }.addOnFailureListener {
                        Log.e("GeneratedImageOutput", "Failed to upload image", it)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun saveImageUrlToFirestore() {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date())

        val userActivityModel = UserActivityModel(
            uploaderId = FirebaseAuth.getInstance().currentUser?.uid!!,
            beforeImageUrl = beforeImageUrl,
            afterImageUrl = afterImageUrl,
            timestamp = formattedDate, // Use the formatted date string
            userActivityId = "${FirebaseAuth.getInstance().currentUser?.uid}_${Timestamp.now()}"
        )

        firestore.collection("UserActivityModel")
            .add(userActivityModel)
            .addOnSuccessListener { documentReference ->
                Log.d("GeneratedImageOutput", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("GeneratedImageOutput", "Error adding document", e)
            }
    }
}
