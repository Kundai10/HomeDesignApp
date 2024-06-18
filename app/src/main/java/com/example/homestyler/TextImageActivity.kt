package com.example.homestyler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar


class TextImageActivity : AppCompatActivity() {
    private lateinit var startDateText: TextView
    private lateinit var endDateText: TextView
    private lateinit var spinner: Spinner
    private lateinit var yourImage :ImageView
    private lateinit var progressbar: ProgressBar
    private lateinit var loadingTextView: TextView
    private lateinit var textImageEditTextView: TextView
    private val snapAndDesignViewModel: SnapAndDesignViewModel by viewModels()

    // I have 6 images labelled,
    // kitchen, bedroom, bathroom,living_room, dining_room, office


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_image)
        startDateText = findViewById(R.id.startDateTextView)
        endDateText = findViewById(R.id.endDateTextView)
        spinner = findViewById(R.id.spinner)
        yourImage = findViewById(R.id.yourImageView)
        progressbar = findViewById(R.id.progressBar7)
        loadingTextView = findViewById(R.id.loadingTextView3)
        textImageEditTextView = findViewById(R.id.etDesignPrompt)

        // Initialize your drawable names list
        val drawableNames =
            listOf("kitchen", "bedroom", "bathroom", "living_room", "dining_room", "office")

        // Use a coroutine to upload drawables without blocking the UI thread
        CoroutineScope(Dispatchers.IO).launch {
            drawableNames.forEach { drawableName ->
                uploadDrawableToFirebaseStorage(drawableName)
            }
        }

        calculateAndFormatDates()

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.room_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
            // Set the spinner's selection to -1 to make no item selected
            spinner.setSelection(-1)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Check if the EditText is empty
                if (position > -1 && textImageEditTextView.text.isEmpty()) {
                    // Display a Toast message asking the user to input text first
                    Toast.makeText(this@TextImageActivity, "Please input prompt first", Toast.LENGTH_SHORT).show()
                    // Return from the method to prevent any further action
                    return
                }

                val selectedRoom = parent.getItemAtPosition(position).toString()
                // Convert the spinner content to the correct format
                val firebaseName = mapSpinnerContentToFirebaseName(selectedRoom)
                fetchImageFromFirestore(firebaseName)
                loadingTextView.visibility = View.VISIBLE
                progressbar.visibility = View.VISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // add viewmodel code here
        snapAndDesignViewModel.predictionResponse.observe(this) { response ->

            Log.d("RESPONSE STATUS", response.status)

            Log.d("URL", response.urls.get)

            val url = response.urls.get
            snapAndDesignViewModel.getResult(url)
        }
        snapAndDesignViewModel.getResultResponse.observe(this) { response ->


            if (response.output.isNotEmpty()) {
                // Rename the local variable to avoid conflict
                val outputImageUrl = if (response.output.size > 1) response.output[1] else response.output[0]
                Glide.with(this)
                    .load(outputImageUrl)
                    .into(yourImage)
                loadingTextView.visibility = View.GONE
                progressbar.visibility = View.GONE
            } else {
                Log.e("GeneratedImageOutput", "Failed to get result: ${response.error}")
                Toast.makeText(this, "Failed to get URL result", Toast.LENGTH_SHORT).show()
                // Maybe go to the design screen again??
            }
        }

        }

    fun calculateAndFormatDates() {
        val calendar = Calendar.getInstance()

        // Calculate the end date as 30 days from the current date
        calendar.add(Calendar.DAY_OF_MONTH, 30)

        // Format the dates in the "day.month.year" format
        val formatter = SimpleDateFormat("d.M.yyyy")
        val formattedStartDate = formatter.format(calendar.time)

        // Clone the calendar to avoid modifying the start date
        val clonedCalendar = calendar.clone() as Calendar
        clonedCalendar.add(Calendar.DAY_OF_MONTH, -30) // rewind to original date
        val formattedEndDate = formatter.format(clonedCalendar.time)
        // Update the TextViews with the formatted dates
        startDateText.text = "Free Trial Start: $formattedEndDate"
        endDateText.text = "Free Trial End: $formattedStartDate"
    }
    fun uploadDrawableToFirebaseStorage(drawableName: String) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("siximages/$drawableName.png")
        val drawableId = resources.getIdentifier(drawableName, "drawable", packageName)
        val bitmap = BitmapFactory.decodeResource(resources, drawableId)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Save the download URL to Firestore
                saveDownloadUrlToFirestore(drawableName, uri.toString())
            }
        }.addOnFailureListener {
            // Handle unsuccessful uploads
        }
    }

    fun saveDownloadUrlToFirestore(drawableName: String, downloadUrl: String) {
        val db = Firebase.firestore
        val imageData = hashMapOf(
            "name" to drawableName,
            "url" to downloadUrl
        )

        db.collection("siximages")
            .document(drawableName)
            .set(imageData)
            .addOnSuccessListener {
                Log.d("TextImage", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("TextImage", "Error writing document", e)
            }
    }
    fun fetchImageFromFirestore(drawableName: String) {
        val db = Firebase.firestore
        db.collection("siximages")
            .document(drawableName)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val url = document.getString("url")
                    if (url != null) {
                        snapAndDesignViewModel.makePrediction(imageUrl = url, promptText = textImageEditTextView.text.toString()  )
                    }
                } else {
                    Log.d("TextImage", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TextImage", "get failed with ", exception)
            }


    }
    fun mapSpinnerContentToFirebaseName(spinnerContent: String): String {
        // Convert "Kitchen" to "kitchen", "Living Room" to "living_room", etc.
        return spinnerContent.toLowerCase().replace(" ", "_")
    }

}