package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.adapter.CenterAdapter
import com.masterapp.queueeaseapp.home.HomeUiState
import com.masterapp.queueeaseapp.home.HomeViewModel
import kotlinx.coroutines.launch

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button
    private lateinit var adapter: CenterAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        recyclerView = findViewById(R.id.recyclerCenters)
        progressBar = findViewById(R.id.progressCenters)
        tvError = findViewById(R.id.tvCentersError)
        btnRetry = findViewById(R.id.btnCentersRetry)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CenterAdapter { center ->
            val intent = Intent(this, QueueListActivity::class.java).apply {
                putExtra("centerId", center.id)
                putExtra("centerName", center.name ?: "Center")
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        btnRetry.setOnClickListener { viewModel.fetchCenters() }
        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HomeUiState.Loading -> renderLoading()
                        is HomeUiState.Success -> renderSuccess(state)
                        is HomeUiState.Error -> renderError(state.message)
                    }
                }
            }
        }
    }

    private fun renderLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun renderSuccess(state: HomeUiState.Success) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
        adapter.submitList(state.centers)
    }

    private fun renderError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        btnRetry.visibility = View.VISIBLE
        tvError.text = message
    }
}