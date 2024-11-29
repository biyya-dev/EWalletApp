package com.example.relational.database.sample.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.relational.database.sample.databinding.ActivityShowImageBinding
import com.github.chrisbanes.photoview.PhotoView

class ShowImage : AppCompatActivity() {

    private lateinit var binding: ActivityShowImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        // Find the PhotoView in your layout
        val photoView: PhotoView = binding.photoView

        // Load the bitmap into the PhotoView using Glide
        Glide.with(this)
            .load(DisplayImages.imageBitmap)
            .into(photoView)



        binding.backButton.setOnClickListener(){

            onBackPressed()

        }

    }


}