package com.kdr.nuven

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kdr.nuven.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DataManager.loadState.observe(this) { result ->
            when (result) {
                is LoadResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorMessage.visibility = View.GONE
                }
                is LoadResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is LoadResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorMessage.visibility = View.VISIBLE
                    binding.errorMessage.text = result.message
                }
            }
        }

        DataManager.loadInitialData()
    }
}
