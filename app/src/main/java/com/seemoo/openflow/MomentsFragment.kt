package com.seemoo.openflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seemoo.openflow.data.TaskHistoryItem
import com.seemoo.openflow.utilities.Logger
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.seemoo.openflow.utilities.UserIdManager
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MomentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: MomentsAdapter
    private val db = Firebase.firestore
    private lateinit var userIdManager: UserIdManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_moments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userIdManager = UserIdManager(requireContext())
        // Initialize views
        recyclerView = view.findViewById(R.id.task_history_recycler_view)
        emptyState = view.findViewById(R.id.empty_state)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MomentsAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load task history
        loadTaskHistory()
    }

    private fun loadTaskHistory() {
        val userId = userIdManager.getOrCreateUserId()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    val taskHistoryData = document.get("taskHistory") as? List<Map<String, Any>>
                    if (taskHistoryData != null && taskHistoryData.isNotEmpty()) {
                        val taskHistory = taskHistoryData.mapNotNull { taskData ->
                            try {
                                TaskHistoryItem(
                                    task = taskData["task"] as? String ?: "",
                                    status = taskData["status"] as? String ?: "",
                                    startedAt = taskData["startedAt"] as? Timestamp,
                                    completedAt = taskData["completedAt"] as? Timestamp,
                                    success = taskData["success"] as? Boolean,
                                    errorMessage = taskData["errorMessage"] as? String
                                )
                            } catch (e: Exception) {
                                Logger.e("MomentsFragment", "Error parsing task history item", e)
                                null
                            }
                        }

                        // Sort by startedAt in descending order (most recent first)
                        val sortedTaskHistory = taskHistory.sortedByDescending {
                            it.startedAt?.toDate() ?: java.util.Date(0)
                        }

                        if (sortedTaskHistory.isNotEmpty()) {
                            showTaskHistory(sortedTaskHistory)
                        } else {
                            showEmptyState()
                        }
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                }
            } catch (e: Exception) {
                Logger.e("MomentsFragment", "Error loading task history", e)
                showEmptyState()
            }
        }
    }

    private fun showTaskHistory(taskHistory: List<TaskHistoryItem>) {
        adapter = MomentsAdapter(taskHistory)
        recyclerView.adapter = adapter
        recyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
}
