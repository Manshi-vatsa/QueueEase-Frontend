package com.masterapp.queueeaseapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.showNotification
import com.masterapp.queueeaseapp.adapter.QueueAdapter
import com.masterapp.queueeaseapp.queue.JoinQueueState
import com.masterapp.queueeaseapp.queue.QueueUiState
import com.masterapp.queueeaseapp.queue.QueueViewModel
import com.masterapp.queueeaseapp.utils.SessionManager
import kotlinx.coroutines.launch

class QueueListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QueueAdapter
    private lateinit var viewModel: QueueViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnJoinQueue: Button
    private lateinit var tvCenterTitle: TextView
    private lateinit var tvCurrentServing: TextView
    private lateinit var tvMyQueueNumber: TextView
    private lateinit var tvPeopleAhead: TextView
    private lateinit var tvWaitTime: TextView
    private lateinit var tvRecommendation: TextView
    private lateinit var tvEmptyQueue: TextView
    private var centerId: Long = -1L
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_list)

        centerId = intent.getLongExtra("centerId", -1L)
        userId = SessionManager.getUserId() ?: -1L
        if (centerId == -1L || userId == -1L) {
            Toast.makeText(this, "Invalid center or user session.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        recyclerView = findViewById(R.id.recyclerViewQueue)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = QueueAdapter()
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[QueueViewModel::class.java]
        btnRetry.setOnClickListener { viewModel.loadQueue(userId, centerId) }
        btnJoinQueue.setOnClickListener { viewModel.joinQueue(userId, centerId) }
        observeUiState()
        observeJoinState()
        viewModel.loadQueue(userId, centerId)
    }

    override fun onStart() {
        super.onStart()
        if (centerId != -1L && userId != -1L) {
            viewModel.startAutoRefresh(userId, centerId)
        }
    }

    override fun onStop() {
        viewModel.stopAutoRefresh()
        super.onStop()
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progressQueue)
        tvError = findViewById(R.id.tvQueueError)
        btnRetry = findViewById(R.id.btnQueueRetry)
        btnJoinQueue = findViewById(R.id.btnJoinQueue)
        tvCenterTitle = findViewById(R.id.tvCenterTitle)
        tvCurrentServing = findViewById(R.id.tvCurrentServing)
        tvMyQueueNumber = findViewById(R.id.tvMyQueueNumber)
        tvPeopleAhead = findViewById(R.id.tvPeopleAhead)
        tvWaitTime = findViewById(R.id.tvWaitTime)
        tvRecommendation = findViewById(R.id.tvRecommendation)
        tvEmptyQueue = findViewById(R.id.tvQueueEmpty)
        tvCenterTitle.text = intent.getStringExtra("centerName") ?: "Queue Details"
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is QueueUiState.Loading -> renderLoading()
                        is QueueUiState.Error -> renderError(state.message)
                        is QueueUiState.Empty -> {
                            renderContent()
                            bindStatus(
                                currentServing = state.status?.currentServing ?: 0,
                                queueNumber = state.status?.queueNumber ?: 0,
                                peopleAhead = state.status?.peopleAhead ?: 0,
                                estimatedWaitTime = state.status?.estimatedWaitTime ?: 0,
                                recommendation = state.status?.recommendation ?: "-"
                            )
                            tvEmptyQueue.visibility = View.VISIBLE
                            adapter.submitList(emptyList())
                        }

                        is QueueUiState.Success -> {
                            renderContent()
                            tvEmptyQueue.visibility = View.GONE
                            bindStatus(
                                currentServing = state.data.status.currentServing,
                                queueNumber = state.data.status.queueNumber,
                                peopleAhead = state.data.status.peopleAhead,
                                estimatedWaitTime = state.data.status.estimatedWaitTime,
                                recommendation = state.data.status.recommendation
                            )
                            adapter.submitList(state.data.users)
                            if (state.data.status.peopleAhead <= 5 ||
                                state.data.status.estimatedWaitTime <= 10
                            ) {
                                showNotification(this@QueueListActivity)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeJoinState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.joinState.collect { state ->
                    when (state) {
                        is JoinQueueState.Idle -> btnJoinQueue.isEnabled = true
                        is JoinQueueState.Loading -> btnJoinQueue.isEnabled = false
                        is JoinQueueState.Success -> {
                            btnJoinQueue.isEnabled = true
                            Toast.makeText(
                                this@QueueListActivity,
                                "Joined queue. Your number: ${state.booking.queueNumber}",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetJoinState()
                        }

                        is JoinQueueState.Error -> {
                            btnJoinQueue.isEnabled = true
                            Toast.makeText(this@QueueListActivity, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetJoinState()
                        }
                    }
                }
            }
        }
    }

    private fun bindStatus(
        currentServing: Int,
        queueNumber: Int,
        peopleAhead: Int,
        estimatedWaitTime: Int,
        recommendation: String
    ) {
        tvCurrentServing.text = "Current serving: $currentServing"
        tvMyQueueNumber.text = "Your queue number: $queueNumber"
        tvPeopleAhead.text = "People ahead: $peopleAhead"
        tvWaitTime.text = "Estimated wait: $estimatedWaitTime min"
        tvRecommendation.text = "Recommendation: $recommendation"
    }

    private fun renderLoading() {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun renderError(message: String) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = message
        btnRetry.visibility = View.VISIBLE
    }

    private fun renderContent() {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }
}