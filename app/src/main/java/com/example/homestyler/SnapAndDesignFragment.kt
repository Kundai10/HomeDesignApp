package com.example.homestyler

import FirebaseStorageHelper
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.homestyler.model.ImageModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import crocodile8.image_picker_plus.ImagePickerPlus
import crocodile8.image_picker_plus.PickRequest
import crocodile8.image_picker_plus.PickSource


class SnapAndDesignFragment : Fragment() {
    private lateinit var uploadImageCardView: CardView
    private lateinit var yourImage: ImageView
    private lateinit var inputColorScheme: EditText
    private lateinit var inputDesignStyle: EditText
    private lateinit var inputRoomName: EditText
    private lateinit var designButton: ExtendedFloatingActionButton
    private lateinit var userName: TextView
    private lateinit var progressBar: ProgressBar

    // Add a flag to control navigation
    private var shouldNavigateToGeneratedImageOutput = false

    private var selectedImageUri: Uri? = null
    private var isImageUploaded = false

    private val firebaseStorageHelper = FirebaseStorageHelper()
    private val firestoreHelper = FirestoreHelper()

    private val snapAndDesignViewModel: SnapAndDesignViewModel by viewModels()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
                showImageUploaded()
            }
        }

    private fun showImageUploaded() {
        // method to show the image in the image view
        selectedImageUri?.let {
            Glide.with(yourImage).load(it).into(yourImage)
        }
        // image is posted to firebase.
        //This happens before the user types in the textfields
        progressBar.visibility = View.VISIBLE
        postImage()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_snap_and_design, container, false)
        uploadImageCardView = view.findViewById(R.id.uploadImageCardView)
        yourImage = view.findViewById(R.id.yourImage)
        inputColorScheme = view.findViewById(R.id.etColorScheme)
        inputDesignStyle = view.findViewById(R.id.etDesignStyle)
        inputRoomName = view.findViewById(R.id.etRoomName)
        designButton = view.findViewById(R.id.designButton)
        userName = view.findViewById(R.id.tvUserName)
        progressBar = view.findViewById(R.id.progress_bar3)

        //greet the user with their name
        fetchUsernameAndUpdateWelcomeMessage()

        //If user clicks on the CardView,they have an option to select from gallery or take a photo
        uploadImageCardView.setOnClickListener {
            //user selects image from gallery
            showAlertDialog()

        }

        // Text field will be disabled if the image is not uploaded to Firebase
        setUpEditTextField()

        return view
    }


    private fun setUpEditTextField() {
        setUpEditText(inputRoomName)
        setUpEditText(inputDesignStyle)
        setUpEditText(inputColorScheme)
    }

    private fun setUpEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Check if EditText has focus before showing toast
                if (!isImageUploaded && editText.hasFocus()) {
                    Toast.makeText(activity, "Please select an image first", Toast.LENGTH_SHORT)
                        .show()
                    editText.text.clear() // Clear text if image is not uploaded
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used in this case
            }
        })
    }


    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_image_picker, null)

        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.camera_button).setOnClickListener {
            ImagePickerPlus.createIntent(
                activity = requireActivity(),
                PickRequest(
                    source = PickSource.CAMERA,
                )
            ).let { launcher.launch(it) }
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.gallery_button).setOnClickListener {
            ImagePickerPlus.createIntent(
                activity = requireActivity(),
                PickRequest(
                    source = PickSource.GALLERY,
                )
            ).let { launcher.launch(it) }
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun postImage() {
        selectedImageUri?.apply {
            firebaseStorageHelper.uploadImage(
                this,
                onSuccess = { url ->
                    postToFirestore(url)
                },
                onFailure = {
                    Toast.makeText(
                        activity,
                        "Something went wrong.It's not you.It's us",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun postToFirestore(url: String) {
        val imageModel = ImageModel(
            url,
            "${FirebaseAuth.getInstance().currentUser?.uid}_${Timestamp.now()}",
            FirebaseAuth.getInstance().currentUser?.uid!!,
            Timestamp.now(),
        )
        firestoreHelper.uploadImageToFirestore(
            imageModel,
            onSuccess = {
                //Toast message saying upload successful and then enable button
                Toast.makeText(activity, "Image Upload Successful", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                isImageUploaded = true
                //if edittext fields aren't empty then a user can click upload button
                designButton.setOnClickListener {
                    if (inputDesignStyle.text.isEmpty() || inputRoomName.text.isEmpty() || inputColorScheme.text.isEmpty()) {
                        Toast.makeText(activity, "Fill in all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        updateApiWithImageUrl(url)
                        shouldNavigateToGeneratedImageOutput = true
                    }

                }
            },
            onFailure = {
                //toast message saying something went wrong
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun fetchUsernameAndUpdateWelcomeMessage() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            firestoreHelper.getUsername(userId,
                onSuccess = { username ->
                    // Update tvWelcomeMessage with the retrieved username
                    val welcomeMessage = getString(R.string.welcomeMessage, username)
                    userName.text = welcomeMessage
                },
                onFailure = {
                    // Handle failure to retrieve username
                    val welcomeMessage = getString(R.string.welcomeMessage, "Buddy")
                    userName.text = welcomeMessage
                }
            )
        }
    }


    private fun updateApiWithImageUrl(imageUrl: String) {

        // Collect the user's input
        val colorScheme = inputColorScheme.text.toString()
        val designStyle = inputDesignStyle.text.toString()
        val roomName = inputRoomName.text.toString()

        // The variable containing the validated user's combined input.
        val userInput = "$colorScheme $designStyle $roomName"

        // Update API with the image URL and the validated user input
        snapAndDesignViewModel.makePrediction(imageUrl = imageUrl, promptText = userInput)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observe the state here always in onViewCreated,
        snapAndDesignViewModel.predictionResponse.observe(viewLifecycleOwner) { response ->


            // Navigate to the new fragment and pass the URL as a parameter
            // Create intent to launch the activity
            if (shouldNavigateToGeneratedImageOutput) {
                // Navigate to the new fragment and pass the URL as a parameter
                Log.d("RESPONSE STATUS", response.status)
                Toast.makeText(activity, "Response is ${response.status}", Toast.LENGTH_LONG).show()

                Log.d("URL", response.urls.get)

                val url = response.urls.get

                //snapAndDesignViewModel.getResult(url)
                Log.d("URL", url)

                val intent =
                    Intent(requireContext(), GeneratedImageOutputActivity::class.java).apply {
                        putExtra("url", response.urls.get)
                    }
                startActivity(intent)
                // Reset the flag to prevent repeated navigation
                shouldNavigateToGeneratedImageOutput = false
            }
        }
    }


}