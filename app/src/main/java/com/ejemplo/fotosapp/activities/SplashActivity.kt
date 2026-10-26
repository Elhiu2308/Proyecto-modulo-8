package com.ejemplo.fotosapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ejemplo.fotosapp.databinding.ActivitySplashBinding
import com.ejemplo.fotosapp.services.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sesion.haySesion()) {
                startActivity(Intent(this, PrincipalActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}
