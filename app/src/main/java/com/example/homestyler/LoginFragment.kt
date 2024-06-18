package com.example.homestyler

import android.app.AlertDialog
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
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException


class LoginFragment : Fragment() {

    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var lEmail: EditText
    private lateinit var lPassword: EditText
    private lateinit var resetPassword: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Check if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            // If user is already logged in, navigate to HomeScreen
            startActivity(Intent(requireActivity(), HomeScreenActivity::class.java))
            // Finish the current activity to prevent going back to LoginFragment
            requireActivity().finish()
            return null // No need to inflate the fragment's layout
        }

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        loginButton = view.findViewById(R.id.sign_in_button)
        signupButton = view.findViewById(R.id.sign_up_button)
        lEmail = view.findViewById(R.id.etLoginEmailField)
        lPassword = view.findViewById(R.id.etLoginPasswordField)
        resetPassword = view.findViewById(R.id.forgot_password_text)
        progressBar = view.findViewById(R.id.progress_bar)

        signupButton.setOnClickListener {
            // navigate to register page when user clicks on text
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.registerFragment)
        }

        loginButton.setOnClickListener {
            login()
        }
        resetPassword.setOnClickListener {
            showResetPasswordDialog()
        }


        return view
    }
    private fun setInProgress(inProgress : Boolean){
        if(inProgress){
            progressBar.visibility = View.VISIBLE
            signupButton.visibility = View.GONE
            loginButton.visibility = View.GONE

        }else{
            progressBar.visibility = View.GONE
            signupButton.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE

        }
    }

    private fun login() {
        val email = lEmail.text.toString()
        val password = lPassword.text.toString()
        //set up validations
        if (email.isEmpty() || password.isEmpty()) {
            // return a toast that prompts user to text something
            Toast.makeText(activity, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            lPassword.error = "Email not valid"
            return
        }
        setInProgress(true)
        loginWithFirebase(email, password)
    }

    private fun loginWithFirebase(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setInProgress(false)
                startActivity(Intent(requireActivity(), HomeScreenActivity::class.java))
            }.addOnFailureListener { e ->
                setInProgress(false)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(activity, "No account found with this email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)
        val resetButton = dialogView.findViewById<Button>(R.id.resetButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }
    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }


}