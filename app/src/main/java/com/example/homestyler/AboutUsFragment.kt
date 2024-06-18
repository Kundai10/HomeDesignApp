package com.example.homestyler

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView


class AboutUsFragment : Fragment() {
    private lateinit var textviewCardView: CardView
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about_us, container, false)

        textviewCardView = view.findViewById(R.id.textToImageCardView)
        // Set up the click listener
        textviewCardView.setOnClickListener {
            showDialog(it.context)
        }
        return view
    }
    private fun showDialog(context: Context) {
        // Inflate the dialog layout
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_premium, null)
        builder.setView(dialogView)

        // Set up the dialog buttons
        val continueButton: Button = dialogView.findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            // Dismiss the dialog before navigating to TextImageActivity
            dialog.dismiss()

            // Navigate to TextImageActivity
            val intent = Intent(activity, TextImageActivity::class.java)
            startActivity(intent)
        }

        val cancelButton: Button = dialogView.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            // Close the dialog
            dialog.dismiss()
        }

        // Create the dialog
        dialog = builder.create() // Assign the dialog to the class-level variable
        // Show the dialog
        dialog.show()
    }




}