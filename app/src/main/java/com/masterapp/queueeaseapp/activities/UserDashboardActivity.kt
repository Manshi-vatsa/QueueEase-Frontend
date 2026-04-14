package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.adapter.CenterAdapter
import com.masterapp.queueeaseapp.api.ApiClient
import com.masterapp.queueeaseapp.model.BookingResponse
import com.masterapp.queueeaseapp.model.CenterResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var userId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        val pref = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        userId = pref.getLong("USER_ID", 0L)

        recyclerView = findViewById(R.id.recyclerCenters)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadCenters()
    }

    private fun loadCenters() {

        ApiClient.apiService.getCenters().enqueue(object : Callback<List<CenterResponse>> {

            override fun onResponse(
                call: Call<List<CenterResponse>>,
                response: Response<List<CenterResponse>>
            ) {
                if (response.isSuccessful) {

                    val centers = response.body() ?: emptyList()

                    val adapter = CenterAdapter(centers) { centerId ->

                        // 🔥 पहले queue join करो
                        joinQueue(centerId)

                    }

                    recyclerView.adapter = adapter

                } else {
                    Toast.makeText(this@UserDashboardActivity, "Error loading centers", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CenterResponse>>, t: Throwable) {
                Toast.makeText(this@UserDashboardActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔥 JOIN QUEUE + OPEN NEXT SCREEN
    private fun joinQueue(centerId: Long) {

        ApiClient.apiService.joinQueue(userId, centerId)
            .enqueue(object : Callback<BookingResponse> {

                override fun onResponse(
                    call: Call<BookingResponse>,
                    response: Response<BookingResponse>
                ) {

                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@UserDashboardActivity,
                            "Joined Queue ✅",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 🔥 FIXED DELAY + SAFE NAVIGATION
                        android.os.Handler(mainLooper).postDelayed({

                            val intent = Intent(this@UserDashboardActivity, QueueListActivity::class.java)
                            intent.putExtra("centerId", centerId)
                            startActivity(intent)

                        }, 800)

                    } else {
                        Toast.makeText(this@UserDashboardActivity, "Join Failed ❌", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                    Toast.makeText(this@UserDashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}