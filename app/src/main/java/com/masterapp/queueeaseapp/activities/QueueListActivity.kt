package com.masterapp.queueeaseapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.adapter.QueueAdapter
import com.masterapp.queueeaseapp.api.RetrofitClient
import com.masterapp.queueeaseapp.model.QueueUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QueueListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QueueAdapter
    private val list = mutableListOf<QueueUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("QUEUE_DEBUG", "Activity Opened")

        setContentView(R.layout.activity_queue_list)

        recyclerView = findViewById(R.id.recyclerViewQueue)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = QueueAdapter(list)
        recyclerView.adapter = adapter

        val centerId = intent.getLongExtra("centerId", -1)
        Log.d("QUEUE_DEBUG", "CenterId: $centerId")

        if (centerId == -1L) {
            Toast.makeText(this, "Invalid Center ID ❌", Toast.LENGTH_SHORT).show()
            return
        }

        loadQueue(centerId)
    }

    private fun loadQueue(centerId: Long) {

        Log.d("QUEUE_DEBUG", "API HIT")

        RetrofitClient.api.getQueueList(centerId)
            .enqueue(object : Callback<List<QueueUser>> {

                override fun onResponse(
                    call: Call<List<QueueUser>>,
                    response: Response<List<QueueUser>>
                ) {

                    Log.d("QUEUE_DEBUG", "Response Code: ${response.code()}")

                    if (response.isSuccessful) {

                        val data = response.body()

                        if (data != null && data.isNotEmpty()) {

                            Log.d("QUEUE_DEBUG", "Data: $data")

                            list.clear()
                            list.addAll(data)
                            adapter.notifyDataSetChanged()

                        } else {
                            Toast.makeText(this@QueueListActivity, "Queue Empty ❌", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@QueueListActivity, "Server Error ❌", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<QueueUser>>, t: Throwable) {
                    Log.d("QUEUE_DEBUG", "Error: ${t.message}")
                    Toast.makeText(this@QueueListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}