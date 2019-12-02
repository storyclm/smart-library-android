package ru.breffi.smartlibrary.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import ru.breffi.smartlibrary.host.HostActivity

class SplashActivity : Activity() {

    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler.postDelayed(
            {
                startActivity(Intent(this, HostActivity::class.java))
                finish()
            },
            2000
        )
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}