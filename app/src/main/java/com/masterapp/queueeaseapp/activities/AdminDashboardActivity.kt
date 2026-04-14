package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.CenterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val btnAdd = findViewById<Button>(R.id.btnAddCenter)
        val btnView = findViewById<Button>(R.id.btnViewQueue)

        // ✅ VIEW QUEUE
        btnView.setOnClickListener {
            val intent = Intent(this, QueueListActivity::class.java)
            intent.putExtra("centerId", 1)
            startActivity(intent)
        }

        // ✅ ADD CENTER
        btnAdd.setOnClickListener {

            val name = findViewById<EditText>(R.id.etCenterName).text.toString()
            val location = findViewById<EditText>(R.id.etLocation).text.toString()
            val type = findViewById<EditText>(R.id.etType).text.toString()

            if (name.isEmpty() || location.isEmpty() || type.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 CORRECT ORDER
            val center = CenterResponse(0, name, location, type)

            RetrofitClient.api.addCenter(center)
                .enqueue(object : Callback<CenterResponse> {

                    override fun onResponse(
                        call: Call<CenterResponse>,
                        response: Response<CenterResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminDashboardActivity, "Center Added ✅", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AdminDashboardActivity, "Failed ❌", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CenterResponse>, t: Throwable) {
                        Toast.makeText(this@AdminDashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}