package com.example.loginvianaver.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.loginvianaver.R
import com.example.loginvianaver.viewmodel.PaymentResultData
import com.example.loginvianaver.viewmodel.ViewModel
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.Event

class WebViewModeActivity : AppCompatActivity() {

    private val viewModel: ViewModel by viewModels()
    private var request: IamPortRequest? = null
    
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
        initData()
        request = viewModel.createIamPortRequest()
    }
    
    private fun onStartData() {
        val userCode = viewModel.userCode
        request?.let { request ->

            webview.let {

                this.onBackPressedDispatcher.addCallback(this, backPressCallback)

                 Iamport.payment(userCode, webviewMode = it, iamPortRequest = request, paymentResultCallback = { it ->
                    // 결제 완료 후 결과 콜백을 토스트 메시지로 보여줌
                  callBackListener.result(it)
                })

                this.request = null // reload 방지
            }
        }

        normalmodeButton?.setOnClickListener {
            Iamport.close()
            popBackStack()
        }
    }
    
    private fun initData(){
        webview = findViewById(R.id.webview)
        normalmodeButton = findViewById(R.id.normalmode_button)
    }

    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            Log.i("SAMPLE", "결제 결과 콜백\n$resJson")
            PaymentResultData.result66 = iamPortResponse

            //popBackStack()
            if (iamPortResponse != null) {
                viewModel.resultCallback.postValue(Event(iamPortResponse))
            }
        }
    }
    fun popBackStack() {
        runCatching {
            //(activity as MainActivity).popBackStack()
        }.onFailure {
            Log.e("WebViewMode", "돌아갈 수 없습니다.")
        }
    }

    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            remove()
            popBackStack()
        }
    }
    
}