package com.example.loginvianaver.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.loginvianaver.R
import com.example.loginvianaver.service.ApiClient
import com.example.loginvianaver.viewmodel.PaymentResultData
import com.example.loginvianaver.viewmodel.ViewModel
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.Payment
import com.navercorp.nid.NaverIdLoginSDK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class PaymentResultActivity : AppCompatActivity() {

    lateinit var resultMessage : TextView

    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val impResponse = PaymentResultData.result66
        val resultText = if (isSuccess(impResponse)) "결제성공" else "결제실패"
        val color = if (isSuccess(impResponse)) R.color.md_green_200 else R.color.fighting


        val accessToken = NaverIdLoginSDK.getAccessToken().toString()

        viewModel.getUserInfo(accessToken,this@PaymentResultActivity){
            if(isSuccess(impResponse)){
                val payment = com.example.loginvianaver.modell.Payment(generateRandomLongInRange(0,1000000).toInt(),it!!,"test")
                val sendDatatPayment = ApiClient.apiService.createPayment(payment)
                sendDatatPayment.enqueue(object : Callback<com.example.loginvianaver.modell.Payment>{
                    override fun onResponse(
                        p0: Call<com.example.loginvianaver.modell.Payment>,
                        p1: Response<com.example.loginvianaver.modell.Payment>
                    ) {

                    }

                    override fun onFailure(
                        p0: Call<com.example.loginvianaver.modell.Payment>,
                        p1: Throwable
                    ) {

                    }

                })
            }
        }

        resultMessage = findViewById(R.id.result_message)

        resultMessage.setTextColor(ContextCompat.getColor(this
        ,color))
    }

    private fun isSuccess(iamPortResponse: IamPortResponse?): Boolean {
        if (iamPortResponse == null) {
            return false
        }
        return iamPortResponse.success == true || iamPortResponse.imp_success == true
    }

    fun generateRandomLongInRange(min: Long, max: Long): Long {
        return Random.nextLong(min, max)
    }
}