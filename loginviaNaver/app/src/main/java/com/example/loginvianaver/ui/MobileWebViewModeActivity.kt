package com.example.loginvianaver.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.loginvianaver.R
import com.example.loginvianaver.viewmodel.MyWebViewChromeClient
import com.example.loginvianaver.viewmodel.MyWebViewClient
import com.example.loginvianaver.viewmodel.ViewModel
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.EventObserver

class MobileWebViewModeActivity : AppCompatActivity() {

    private val viewModel: ViewModel by viewModels()
    private var createdView = false

    lateinit var webview : WebView
    lateinit var normalmodeButton : Button
    private var webmode : View = findViewById(R.id.webview_mode_fragment)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view_mode)
        ViewCompat.setOnApplyWindowInsetsListener(webmode) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createdView = true

        
    }

    override fun onStart() {
        if (!createdView) {
            return
        }

        webview?.let {

            it.webViewClient = MyWebViewClient()
            it.webChromeClient = MyWebViewChromeClient()


            Iamport.mobileWebModeShouldOverrideUrlLoading()?.observe(this, EventObserver { uri ->
                Log.i("SAMPLE", "changed url :: $uri")
            })

            it.loadUrl("https://pay-demo.iamport.kr") // 아임포트 데모 페이지
            Iamport.pluginMobileWebSupporter(it) // 로컬 데모 페이지
            createdView = false
        }

        normalmodeButton?.setOnClickListener {
            Iamport.close()
            popBackStack()
        }
        super.onStart()
    }

    fun popBackStack() {
        runCatching {
            popBackStack()
        }.onFailure {
        }
    }
}