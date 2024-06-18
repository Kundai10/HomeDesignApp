package com.example.homestyler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class HomeScreenActivity : AppCompatActivity() {
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var profileIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        profileIcon = findViewById(R.id.icon_top_right)
        profileIcon.setOnClickListener {
            showProfileOptionsDialog()
        }

        // Get the NavController
        navController = Navigation.findNavController(this, R.id.nav_host)

        // Set up the BottomNavigationView with NavController
        bottomNavView = findViewById(R.id.bottom_nav_view)
        bottomNavView.setupWithNavController(navController)
        // Listen for navigation changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Change background color based on destination
            when (destination.id) {
                R.id.historyFragment -> {
                    bottomNavView.setBackgroundColor(
                        ContextCompat.getColor(
                            this@HomeScreenActivity,
                            R.color.purple
                        )
                    )
                }

                R.id.aboutUsFragment -> {
                    bottomNavView.setBackgroundColor(
                        ContextCompat.getColor(
                            this@HomeScreenActivity,
                            R.color.blue
                        )
                    )
                }

                R.id.snapAndDesignFragment -> {
                    bottomNavView.setBackgroundColor(
                        ContextCompat.getColor(this@HomeScreenActivity, R.color.green)
                    )
                }

                else -> {
                    // Reset to default background color
                    bottomNavView.setBackgroundColor(
                        ContextCompat.getColor(
                            this@HomeScreenActivity,
                            R.color.darkGray
                        )
                    )
                }
            }
        }

    }
    //deprecation warnings. Might need to find alternatives
    private fun showProfileOptionsDialog() {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile_options, null)

        // Find the buttons in the dialog layout
        val logoutButton = dialogView.findViewById<Button>(R.id.logout_button)
        val deleteAccountButton = dialogView.findViewById<Button>(R.id.delete_account_button)

        // Set up the click listeners for the buttons
        logoutButton.setOnClickListener {
            logout()
        }
        deleteAccountButton.setOnClickListener {
            deleteAccount()
        }

        // Create the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        // navigate the user to the login screen
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // User account deleted successfully
                // Navigate the user to the login screen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


