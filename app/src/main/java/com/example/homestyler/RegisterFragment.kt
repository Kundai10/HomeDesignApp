package com.example.homestyler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.homestyler.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {

    private lateinit var registerButton: Button
    private lateinit var rUsername: EditText
    private lateinit var rEmail: EditText
    private lateinit var rPassword: EditText
    private lateinit var rConfirmPassword: EditText
    private lateinit var signinButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        registerButton = view.findViewById(R.id.registerSignUpButton)
        rUsername = view.findViewById(R.id.etRegisterNameField)
        rEmail = view.findViewById(R.id.etRegisterEmailField)
        rConfirmPassword = view.findViewById(R.id.etRegisterConfirmPasswordField)
        rPassword = view.findViewById(R.id.etRegisterPasswordField)
        signinButton = view.findViewById(R.id.registerSignInButton)
        progressBar = view.findViewById(R.id.progress_bar)

        signinButton.setOnClickListener {
            // go to login screen when user clicks on text
            val navController = view.findNavController()
            navController.navigate(R.id.action_registerFragment_to_loginFragment)
        }

        registerButton.setOnClickListener {
            createAccount()
        }


        return view
    }
    private fun setInProgress(inProgress : Boolean){
        if(inProgress){
            progressBar.visibility = View.VISIBLE
            registerButton.visibility = View.GONE
            signinButton.visibility = View.GONE

        }else{
            progressBar.visibility = View.GONE
            registerButton.visibility = View.VISIBLE
            signinButton.visibility = View.VISIBLE

        }
    }

    private fun createAccount() {
        val email = rEmail.text.toString()
        val password = rPassword.text.toString()
        val confirmPassword = rConfirmPassword.text.toString()
        val username = rUsername.text.toString()
        // set up account validations,eg the passwords have to match, etc.
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            // return a toast that prompts user to text something
            Toast.makeText(activity, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            rConfirmPassword.error = "Passwords must match"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            rEmail.error = "Email not valid"
            return
        }
        setInProgress(true)
        signUpWithFirebase(email, password)
    }

    private fun signUpWithFirebase(email: String, password: String) {
        val username = rUsername.text.toString()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.let { user ->
                    val userModel = UserModel(user.uid, email, username)
                    Firebase.firestore.collection("users")
                        .document(user.uid)
                        .set(userModel).addOnSuccessListener {
                            // go to the snap and design screen
                            setInProgress(false)
                            startActivity(Intent(requireActivity(), HomeScreenActivity::class.java))
                        }.addOnFailureListener { exception ->
                            Log.e("REGISTER", "Error setting user data: ${exception.message}")
                            Toast.makeText(activity, "Error setting user data", Toast.LENGTH_SHORT).show()
                            setInProgress(false)
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle other types of failures
                if (exception is FirebaseAuthUserCollisionException) {
                    // Log the error
                    Log.e("REGISTER", "User already exists: ${exception.message}")
                    // Display a toast or handle the error as needed
                    Toast.makeText(activity, "User already exists", Toast.LENGTH_SHORT).show()
                } else {
                    // Display a generic error toast
                    Log.e("REGISTER", "Authentication failed: ${exception.message}")
                    Toast.makeText(activity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
                setInProgress(false)
            }
    }





}
