package com.example.relational.database.sample.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.relational.database.sample.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {


    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}