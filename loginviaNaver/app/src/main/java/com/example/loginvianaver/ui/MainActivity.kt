package com.example.loginvianaver.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.loginvianaver.R
import com.iamport.sdk.BuildConfig
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.Iamport
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.oauth.view.NidOAuthLoginButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Iamport.create(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }

        val clientId = getString(R.string.OAUTH_CLIENT_ID)
        val clientSecret = getString(R.string.OAUTH_CLIENT_SECRET)
        val clientName = getString(R.string.OAUTH_CLIENT_NAME)

        NaverIdLoginSDK.initialize(this, clientId, clientSecret, clientName)
        val btn = findViewById<NidOAuthLoginButton>(R.id.buttonOAuthLoginImg)
        btn.setOAuthLogin(object : OAuthLoginCallback {
            override fun onError(errorCode: Int, message: String) {
                Log.e("NaverLogin", "onError: $message")
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e("NaverLogin", "onFailure: $message")
            }

            override fun onSuccess() {
                Log.d("NaverLogin", "success")
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
            }
        })


    }


}
