package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure SearchTrainActivity opens first on startup
        startActivity(Intent(this, SearchTrainActivity::class.java))
        finish()
    }
}
