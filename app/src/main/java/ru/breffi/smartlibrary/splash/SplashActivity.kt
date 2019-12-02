package ru.breffi.smartlibrary.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import ru.breffi.smartlibrary.host.HostActivity

class SplashActivity : Activity() {

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Fixing https://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes/23220151
        if (!isTaskRoot
            && intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true
            && intent.action?.equals(Intent.ACTION_MAIN) == true
        ) {
            finish()
            return
        }

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