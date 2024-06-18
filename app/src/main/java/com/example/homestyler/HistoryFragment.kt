package com.example.homestyler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.graphics.Color
import android.widget.Toast
import com.example.homestyler.model.DateItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dateAdapter: HistoryAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val dates = mutableListOf<DateItem>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchDatesFromFirebase()

        return view
    }

    private fun fetchDatesFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        if (currentUserId != null) {
            firestore.collection("UserActivityModel")
                .whereEqualTo("uploaderId", currentUserId)
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val date = document.getString("timestamp") ?: ""
                        val userActivityId = document.getString("userActivityId") ?: ""
                        dates.add(DateItem(date, userActivityId))
                    }
                    // Initialize the adapter here, after dates is populated
                    dateAdapter = HistoryAdapter(dates) { dateItem, action ->
                        when (action) {
                            is Action.Click -> {
                                // Start ImageDetailActivity and pass the selected date and userActivityId
                                val intent = Intent(activity, ImageDetailsActivity::class.java)
                                intent.putExtra("selectedDate", dateItem.date)
                                intent.putExtra("userActivityId", dateItem.userActivityId)
                                startActivity(intent)
                            }

                            is Action.LongPress -> {
                                // Long press action
                                val dialogBuilder = AlertDialog.Builder(requireActivity())
                                    .setTitle("Delete Item")
                                    .setMessage("Are you sure you want to delete this item?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        deleteItemFromFirebaseAndRecyclerView(dateItem)
                                    }
                                    .setNegativeButton("No", null)

                                // Create the AlertDialog
                                val alertDialog = dialogBuilder.create()

                                // Show the AlertDialog
                                alertDialog.show()

                                // Change the text color of the "Yes" and "No" buttons to white
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                                    Color.WHITE
                                )
                                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(Color.WHITE)
                            }

                        }
                    }

                    recyclerView.adapter = dateAdapter
                }
                .addOnFailureListener { exception ->
                    Log.w("History Fragment", "Error getting documents: ", exception)
                }
        } else {
            Log.w("History Fragment", "No current user found")
        }
    }

    private fun deleteItemFromFirebaseAndRecyclerView(dateItem: DateItem) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            // Fetch the document ID based on the userActivityId field
            firestore.collection("UserActivityModel")
                .whereEqualTo("uploaderId", currentUserId)
                .whereEqualTo("userActivityId", dateItem.userActivityId)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.documents.isNotEmpty()) {
                        // there's only one document with this userActivityId for the current user
                        val documentSnapshot = documents.documents[0]
                        val documentId = documentSnapshot.id

                        // Now, use the document ID to update the document
                        val documentReference =
                            firestore.collection("UserActivityModel").document(documentId)
                        documentReference.update("deleted", true)
                            .addOnSuccessListener {
                                // Remove the item from the local list and notify the adapter
                                dates.removeAll { it.userActivityId == dateItem.userActivityId }
                                dateAdapter.notifyDataSetChanged()
                                Toast.makeText(activity, "Deleted!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Log.w(
                                    "History Fragment",
                                    "Error marking item as deleted: ",
                                    exception
                                )
                            }
                    } else {
                        // Document with the specified userActivityId does not exist for the current user
                        Log.w(
                            "History Fragment",
                            "Document with userActivityId ${dateItem.userActivityId} does not exist for the current user."
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("History Fragment", "Error fetching document: ", exception)
                }
        } else {
            Log.w("History Fragment", "No current user found")
        }
    }


}
