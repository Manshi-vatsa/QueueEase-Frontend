package com.masterapp.queueeaseapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.masterapp.queueeaseapp.utils.QueueSimulationEngine
import com.masterapp.queueeaseapp.utils.QueueSnapshot
import com.masterapp.queueeaseapp.utils.SessionManager
import com.masterapp.queueeaseapp.utils.SimulationScenario
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var tvTurnStatus: TextView
    private lateinit var progressQueueUrgency: ProgressBar
    private lateinit var btnServeNext: Button
    private lateinit var tvAdminStats: TextView
    private lateinit var tvSimulationRates: TextView
    private lateinit var tvPredictions: TextView
    private lateinit var tvSuggestion: TextView
    private lateinit var progressCrowdLevel: ProgressBar
    private lateinit var tvCrowdLevel: TextView
    private lateinit var tvQueueGraph: TextView
    private lateinit var layoutWhatIf: View
    private lateinit var btnAddStaffScenario: Button
    private lateinit var btnFasterServiceScenario: Button
    private lateinit var btnResetScenario: Button
    private lateinit var tvWhatIfImpact: TextView
    private lateinit var tvLiveSync: TextView
    private lateinit var tvCrowdPressureValue: TextView
    private lateinit var tvBestJoinWindow: TextView
    private var centerId: Long = -1L
    private var userId: Long = -1L
    private var role: String = "USER"
    private var totalServedToday: Int = 0
    private var cumulativeWaitTimeMinutes: Int = 0
    private val queueHistory = mutableListOf<QueueSnapshot>()
    private var simulationScenario = SimulationScenario()

    private val averageServiceTimePerUserMinutes = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_list)

        centerId = intent.getLongExtra("centerId", -1L)
        userId = SessionManager.getUserId() ?: -1L
        role = intent.getStringExtra("role") ?: "USER"
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
        btnServeNext.setOnClickListener { serveNextUser() }
        btnAddStaffScenario.setOnClickListener {
            simulationScenario = simulationScenario.copy(extraStaff = simulationScenario.extraStaff + 1)
            tvWhatIfImpact.text = "Scenario: +${simulationScenario.extraStaff} staff, ${simulationScenario.speedMultiplier}x speed"
            viewModel.loadQueue(userId, centerId)
        }
        btnFasterServiceScenario.setOnClickListener {
            simulationScenario = simulationScenario.copy(speedMultiplier = simulationScenario.speedMultiplier + 0.25)
            tvWhatIfImpact.text = "Scenario: +${simulationScenario.extraStaff} staff, ${String.format("%.2f", simulationScenario.speedMultiplier)}x speed"
            viewModel.loadQueue(userId, centerId)
        }
        btnResetScenario.setOnClickListener {
            simulationScenario = SimulationScenario()
            tvWhatIfImpact.text = "Impact: Baseline mode"
            viewModel.loadQueue(userId, centerId)
        }
        observeUiState()
        observeJoinState()
        configureRoleUi()
        restoreAdminStats()
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
        tvTurnStatus = findViewById(R.id.tvTurnStatus)
        progressQueueUrgency = findViewById(R.id.progressQueueUrgency)
        btnServeNext = findViewById(R.id.btnServeNext)
        tvAdminStats = findViewById(R.id.tvAdminStats)
        tvSimulationRates = findViewById(R.id.tvSimulationRates)
        tvPredictions = findViewById(R.id.tvPredictions)
        tvSuggestion = findViewById(R.id.tvSuggestion)
        progressCrowdLevel = findViewById(R.id.progressCrowdLevel)
        tvCrowdLevel = findViewById(R.id.tvCrowdLevel)
        tvQueueGraph = findViewById(R.id.tvQueueGraph)
        layoutWhatIf = findViewById(R.id.layoutWhatIf)
        btnAddStaffScenario = findViewById(R.id.btnAddStaffScenario)
        btnFasterServiceScenario = findViewById(R.id.btnFasterServiceScenario)
        btnResetScenario = findViewById(R.id.btnResetScenario)
        tvWhatIfImpact = findViewById(R.id.tvWhatIfImpact)
        tvLiveSync = findViewById(R.id.tvLiveSync)
        tvCrowdPressureValue = findViewById(R.id.tvCrowdPressureValue)
        tvBestJoinWindow = findViewById(R.id.tvBestJoinWindow)
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
                            bindSmartPrediction(
                                peopleAhead = state.status?.peopleAhead ?: 0,
                                estimatedWaitTime = state.status?.estimatedWaitTime ?: 0
                            )
                            bindDigitalTwin(
                                queueLength = 0,
                                peopleAhead = state.status?.peopleAhead ?: 0,
                                currentServing = state.status?.currentServing ?: 0,
                                estimatedWaitTime = state.status?.estimatedWaitTime ?: 0
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
                            bindSmartPrediction(
                                peopleAhead = state.data.status.peopleAhead,
                                estimatedWaitTime = state.data.status.estimatedWaitTime
                            )
                            bindDigitalTwin(
                                queueLength = state.data.users.size,
                                peopleAhead = state.data.status.peopleAhead,
                                currentServing = state.data.status.currentServing,
                                estimatedWaitTime = state.data.status.estimatedWaitTime
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
        val resolvedWaitTime = resolveWaitTime(peopleAhead, estimatedWaitTime)
        tvWaitTime.text = "Estimated wait: $resolvedWaitTime min"
        tvRecommendation.text = "Recommendation: $recommendation"
    }

    private fun bindSmartPrediction(peopleAhead: Int, estimatedWaitTime: Int) {
        val resolvedWaitTime = resolveWaitTime(peopleAhead, estimatedWaitTime)
        val urgencyPercent = when {
            peopleAhead <= 0 -> 100
            peopleAhead == 1 -> 85
            peopleAhead <= 3 -> 70
            peopleAhead <= 6 -> 45
            else -> 20
        }
        progressQueueUrgency.progress = urgencyPercent

        when {
            peopleAhead <= 0 -> {
                tvTurnStatus.text = "Now serving near your token. Please be ready."
                tvTurnStatus.setTextColor(ContextCompat.getColor(this, R.color.danger))
            }

            peopleAhead <= 2 -> {
                tvTurnStatus.text = "Almost your turn (~$resolvedWaitTime min)"
                tvTurnStatus.setTextColor(ContextCompat.getColor(this, R.color.warning))
            }

            else -> {
                tvTurnStatus.text = "Your turn in ~$resolvedWaitTime min"
                tvTurnStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
            }
        }
    }

    private fun resolveWaitTime(peopleAhead: Int, apiEstimatedWaitTime: Int): Int {
        return if (apiEstimatedWaitTime > 0) {
            apiEstimatedWaitTime
        } else {
            peopleAhead * averageServiceTimePerUserMinutes
        }
    }

    private fun configureRoleUi() {
        val isAdmin = role.equals("ADMIN", ignoreCase = true)
        btnJoinQueue.visibility = if (isAdmin) View.GONE else View.VISIBLE
        btnServeNext.visibility = if (isAdmin) View.VISIBLE else View.GONE
        tvAdminStats.visibility = if (isAdmin) View.VISIBLE else View.GONE
        layoutWhatIf.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun serveNextUser() {
        btnServeNext.isEnabled = false
        com.masterapp.queueeaseapp.api.RetrofitClient.api.serveNext(centerId)
            .enqueue(object : retrofit2.Callback<Any> {
                override fun onResponse(call: retrofit2.Call<Any>, response: retrofit2.Response<Any>) {
                    btnServeNext.isEnabled = true
                    if (response.isSuccessful) {
                        val lastShownWaitTime =
                            tvWaitTime.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0
                        totalServedToday += 1
                        cumulativeWaitTimeMinutes += lastShownWaitTime
                        persistAdminStats()
                        renderAdminStats()
                        Toast.makeText(
                            this@QueueListActivity,
                            "Served next user successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.loadQueue(userId, centerId)
                    } else {
                        Toast.makeText(
                            this@QueueListActivity,
                            "Unable to serve next user (${response.code()}).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<Any>, t: Throwable) {
                    btnServeNext.isEnabled = true
                    Toast.makeText(
                        this@QueueListActivity,
                        "Network error while serving next user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun restoreAdminStats() {
        val prefs = getSharedPreferences("admin_stats", MODE_PRIVATE)
        val keyPrefix = "center_$centerId"
        totalServedToday = prefs.getInt("${keyPrefix}_served", 0)
        cumulativeWaitTimeMinutes = prefs.getInt("${keyPrefix}_wait_total", 0)
        renderAdminStats()
    }

    private fun persistAdminStats() {
        val prefs = getSharedPreferences("admin_stats", MODE_PRIVATE)
        val keyPrefix = "center_$centerId"
        prefs.edit()
            .putInt("${keyPrefix}_served", totalServedToday)
            .putInt("${keyPrefix}_wait_total", cumulativeWaitTimeMinutes)
            .apply()
    }

    private fun renderAdminStats() {
        if (!role.equals("ADMIN", ignoreCase = true)) return
        val avgWait = if (totalServedToday == 0) 0 else cumulativeWaitTimeMinutes / totalServedToday
        tvAdminStats.text = "Served today: $totalServedToday | Avg wait: $avgWait min"
    }

    private fun bindDigitalTwin(
        queueLength: Int,
        peopleAhead: Int,
        currentServing: Int,
        estimatedWaitTime: Int
    ) {
        val now = System.currentTimeMillis()
        queueHistory.add(
            QueueSnapshot(
                timestampMs = now,
                queueLength = queueLength,
                currentServing = currentServing
            )
        )
        if (queueHistory.size > 12) {
            queueHistory.removeAt(0)
        }

        val currentWait = resolveWaitTime(peopleAhead, estimatedWaitTime)
        val simulation = QueueSimulationEngine.simulate(
            history = queueHistory,
            currentQueueLength = queueLength,
            currentWaitMinutes = currentWait,
            scenario = simulationScenario
        )

        tvSimulationRates.text = "Arrival rate: ${formatRate(simulation.arrivalRatePerMinute)} users/min | " +
            "Service rate: ${formatRate(simulation.effectiveServiceRatePerMinute)} users/min"
        tvPredictions.text = "Current: ${simulation.currentWaitMinutes} min | " +
            "15m: ${simulation.waitAfter15Minutes} min | " +
            "30m: ${simulation.waitAfter30Minutes} min | " +
            "60m: ${simulation.waitAfter60Minutes} min"

        val suggestionText = if (simulation.waitAfter15Minutes > simulation.currentWaitMinutes) {
            "Suggestion: Predicted crowd increase. Join later."
        } else {
            "Suggestion: Predicted wait is stable/down. Join now."
        }
        tvSuggestion.text = suggestionText
        tvSuggestion.setTextColor(
            ContextCompat.getColor(
                this,
                if (simulation.waitAfter15Minutes > simulation.currentWaitMinutes) R.color.warning else R.color.success
            )
        )

        bindCrowdVisualization(queueLength, simulation.futureQueue15, simulation.futureQueue30, simulation.futureQueue60)
        bindPremiumInsights(simulation)
        renderWhatIfImpact(simulation)
    }

    private fun bindCrowdVisualization(currentQueue: Int, q15: Int, q30: Int, q60: Int) {
        val crowdScore = (currentQueue * 10).coerceIn(0, 100)
        progressCrowdLevel.progress = crowdScore
        when {
            crowdScore >= 75 -> {
                tvCrowdLevel.text = "Crowd level: High"
                tvCrowdLevel.setTextColor(ContextCompat.getColor(this, R.color.danger))
            }

            crowdScore >= 40 -> {
                tvCrowdLevel.text = "Crowd level: Medium"
                tvCrowdLevel.setTextColor(ContextCompat.getColor(this, R.color.warning))
            }

            else -> {
                tvCrowdLevel.text = "Crowd level: Low"
                tvCrowdLevel.setTextColor(ContextCompat.getColor(this, R.color.success))
            }
        }

        tvQueueGraph.text = buildString {
            appendLine("Queue Growth Graph")
            appendLine("Now  ${barForValue(currentQueue)} ($currentQueue)")
            appendLine("15m  ${barForValue(q15)} ($q15)")
            appendLine("30m  ${barForValue(q30)} ($q30)")
            append("60m  ${barForValue(q60)} ($q60)")
        }
    }

    private fun renderWhatIfImpact(simulation: com.masterapp.queueeaseapp.utils.QueueSimulationResult) {
        if (!role.equals("ADMIN", ignoreCase = true)) return
        val baseline = QueueSimulationEngine.simulate(
            history = queueHistory,
            currentQueueLength = queueHistory.lastOrNull()?.queueLength ?: 0,
            currentWaitMinutes = simulation.currentWaitMinutes,
            scenario = SimulationScenario()
        )
        val delta = baseline.waitAfter30Minutes - simulation.waitAfter30Minutes
        tvWhatIfImpact.text = if (simulationScenario.extraStaff == 0 && simulationScenario.speedMultiplier == 1.0) {
            "Impact: Baseline mode"
        } else {
            "Impact: 30m wait improves by ${delta.coerceAtLeast(0)} min (baseline ${baseline.waitAfter30Minutes} -> ${simulation.waitAfter30Minutes})"
        }
    }

    private fun barForValue(value: Int): String {
        val blocks = (value.coerceIn(0, 40) / 2).coerceAtLeast(1)
        return "#".repeat(blocks)
    }

    private fun formatRate(rate: Double): String {
        return String.format("%.2f", rate)
    }

    private fun bindPremiumInsights(simulation: com.masterapp.queueeaseapp.utils.QueueSimulationResult) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        tvLiveSync.text = "Live sync active • Last update: $time"

        val pressureLabel = when {
            simulation.futureQueue30 >= 14 -> "Critical"
            simulation.futureQueue30 >= 8 -> "Elevated"
            simulation.futureQueue30 >= 4 -> "Moderate"
            else -> "Low"
        }
        tvCrowdPressureValue.text = pressureLabel
        tvCrowdPressureValue.setTextColor(
            ContextCompat.getColor(
                this,
                when (pressureLabel) {
                    "Critical" -> R.color.danger
                    "Elevated" -> R.color.warning
                    "Moderate" -> R.color.brand_primary
                    else -> R.color.success
                }
            )
        )

        tvBestJoinWindow.text = if (simulation.waitAfter30Minutes > simulation.currentWaitMinutes) {
            "Next 5-10 min"
        } else {
            "Join now"
        }
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