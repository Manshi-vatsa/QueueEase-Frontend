package com.masterapp.queueeaseapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.masterapp.queueeaseapp.R

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_role_selection)

        val btnAdmin = findViewById<Button>(R.id.btnAdmin)
        val btnUser = findViewById<Button>(R.id.btnUser)

        btnAdmin.setOnClickListener {
            openRegister("ADMIN")
        }

        btnUser.setOnClickListener {
            openRegister("USER")
        }
    }

    private fun openRegister(role: String) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("ROLE", role)
        startActivity(intent)
    }
}