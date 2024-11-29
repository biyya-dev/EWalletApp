package com.example.relational.database.sample.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.content.ContextCompat
import com.example.relational.database.sample.R
import com.example.relational.database.sample.databinding.ActivityRateUsBinding
import com.example.relational.database.sample.extensions.myEmailFeedback

class RateUs : AppCompatActivity() {

    private lateinit var binding:ActivityRateUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRateUsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Change status bar content color to black since status bar color is set to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        binding.btnRateUs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.floatingBtnColor)
        binding.btnLater.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)


        binding.btnRateUs.setOnClickListener(){

            myRateUsDialog()
        }

        binding.btnLater.setOnClickListener(){
            onBackPressed()
        }

        binding.backButton.setOnClickListener(){
            onBackPressed()
        }


    }

    private fun myRateUsDialog() {
        var appRating = 0F
        val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this@RateUs)
        val viewGroup = findViewById<ViewGroup>(R.id.content)
        val dialogView: View = LayoutInflater.from(this@RateUs).inflate(R.layout.layout_ratings,
            viewGroup, false
        )

        // Find buttons within the inflated layout view
        val btnRateUs = dialogView.findViewById<Button>(R.id.btnRateD)
        val btnExit = dialogView.findViewById<Button>(R.id.btnExitt)

        // Set background tint for the buttons
        btnRateUs.backgroundTintList = ContextCompat.getColorStateList(this, R.color.floatingBtnColor)
        btnExit.backgroundTintList = ContextCompat.getColorStateList(this, R.color.floatingBtnColor)

        builder.setView(dialogView)
        val alertDialog: androidx.appcompat.app.AlertDialog = builder.create()

        val ratingbar = dialogView.findViewById<AppCompatRatingBar>(R.id.ratingbarr)
        ratingbar.backgroundTintList =  ContextCompat.getColorStateList(this, R.color.floatingBtnColor)
        ratingbar?.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            appRating = rating
            if (appRating <= 4) {
                btnRateUs.setText("Feedback")
            } else {
                btnRateUs.setText("Rate Us")
            }
        }

        btnRateUs.setOnClickListener {
            if (appRating < 1) {
                Toast.makeText(this@RateUs, "Please Select Stars to rate", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (appRating <= 4) {
                myEmailFeedback(this@RateUs)
            } else {
                val packageName = this@RateUs.packageName
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
            alertDialog.dismiss()
        }

        btnExit.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

}