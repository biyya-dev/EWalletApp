package com.example.relational.database.sample.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.relational.database.sample.databinding.ActivitySettingsBinding
import com.example.relational.database.sample.extensions.myEmailFeedback
import com.example.relational.database.sample.extensions.shareApp

class Settings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get intent
        val intent = intent

        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        //click listeners

        binding.linearOne.setOnClickListener(){

            val intent =  Intent ( this, AboutUs::class.java)
            startActivity(intent)

        }


        binding.linearTwo.setOnClickListener(){
            //share
            shareApp(this@Settings)

        }


        binding.linearThree.setOnClickListener(){

            val intent =  Intent ( this, RateUs::class.java)
            startActivity(intent)

        }


        binding.linearFour.setOnClickListener(){
            // privacy policy

        }


        binding.linearFive.setOnClickListener(){
            // feedback
            myEmailFeedback(this@Settings)

        }

        binding.backButton.setOnClickListener(){
            onBackPressed()
        }

    }
}