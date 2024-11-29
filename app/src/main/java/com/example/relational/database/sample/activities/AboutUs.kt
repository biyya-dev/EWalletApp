package com.example.relational.database.sample.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.relational.database.sample.databinding.ActivityAboutUsBinding

class AboutUs : AppCompatActivity() {

    private lateinit var binding:ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        binding.backButton.setOnClickListener(){
            onBackPressed()
        }

    }
}