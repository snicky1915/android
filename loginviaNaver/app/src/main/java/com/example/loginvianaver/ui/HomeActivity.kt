package com.example.loginvianaver.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.loginvianaver.R
import com.example.loginvianaver.modell.User
import com.example.loginvianaver.receiver.MerchantReceiver
import com.example.loginvianaver.service.ApiClient
import com.example.loginvianaver.viewmodel.PaymentResultData.result66
import com.example.loginvianaver.viewmodel.ViewModel
import com.google.gson.GsonBuilder
import com.iamport.sdk.data.sdk.IamPortCertification
import com.iamport.sdk.data.sdk.IamPortRequest
import com.iamport.sdk.data.sdk.IamPortResponse
import com.iamport.sdk.data.sdk.PG
import com.iamport.sdk.data.sdk.PayMethod
import com.iamport.sdk.domain.core.ICallbackPaymentResult
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST
import com.iamport.sdk.domain.utils.Event
import com.iamport.sdk.domain.utils.EventObserver
import com.iamport.sdk.domain.utils.Util
import com.navercorp.nid.NaverIdLoginSDK
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.Date

class HomeActivity : AppCompatActivity() {

    lateinit var btn: Button
    lateinit var btnLogout : Button
    lateinit var btnPayment : Button
    lateinit var paymentButton : Button
    
    lateinit var payIpayButton : Button
    lateinit var certificationBtn : Button
    lateinit var spinner : Spinner
    lateinit var pgSpinner: Spinner
    lateinit var pgMethod : Spinner
    lateinit var name : EditText
    lateinit var amount : EditText
    lateinit var cardDirectCode : EditText


    private val viewModel: ViewModel by viewModels()

    private val receiver = MerchantReceiver()
    
    lateinit var accessToken: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initAction()
        Iamport.init(this)
        
        loginuser()
        getAlluserInfo()

        registForegroundServiceReceiver(this)

        
   
    }
    
    private fun initAction(){
        btn = findViewById(R.id.btnsenddata)
        paymentButton = findViewById(R.id.paybutton)
        btnLogout = findViewById(R.id.logout)

        payIpayButton = findViewById(R.id.payment_button)
        certificationBtn = findViewById(R.id.certification_button)
        spinner = findViewById(R.id.user_code)
        pgSpinner = findViewById(R.id.pg)
        pgMethod = findViewById(R.id.pg_method)
        name = findViewById(R.id.name)
        amount = findViewById(R.id.amount)
        cardDirectCode = findViewById(R.id.card_direct_code)

        payIpayButton.setOnClickListener {
            onClickPayment()
        }

        certificationBtn.setOnClickListener {
            onClickCertification()
        }

        val userCodeAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            Util.getUserCodeList()
        )

        val pgAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            PG.getPGNames()
        )

        spinner.adapter = userCodeAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.userCode = Util.getUserCode(spinner.selectedItemPosition)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        pgSpinner.adapter = pgAdapter
        pgSpinner.onItemSelectedListener = pgSelectListener

        name.doAfterTextChanged {
            viewModel.paymentName = it.toString()
            println(it.toString())
        }
        name.setText("test payment")
        amount.doAfterTextChanged {
            viewModel.amount = it.toString()
        }
       amount.setText("1000")

        cardDirectCode.doAfterTextChanged {
            viewModel.cardDirectCode = it.toString()
        }

        btnLogout.setOnClickListener {
            NaverIdLoginSDK.logout()
        }
        btn.setOnClickListener {
            getUserInfo(accessToken){
                createnewuser(it!!)
            }
        }
    }

    override fun onStart() {
        viewModel.resultCallback.observe(this, EventObserver {
            startActivity(Intent(this@HomeActivity,PaymentResultActivity::class.java))
        })

        super.onStart()
    }

    private fun onClickPayment() {
        val userCode = viewModel.userCode
        val request = viewModel.createIamPortRequest()
        println(request)

        Iamport.payment(userCode, iamPortRequest = request) { callBackListener.result(it) }
    }

    private fun onClickWebViewModePayment() {
        val userCode = viewModel.userCode
        val request = viewModel.createIamPortRequest()
        Log.i("SAMPLE", "userCode :: $userCode")
        Log.i("SAMPLE", GsonBuilder().setPrettyPrinting().create().toJson(request))

        Iamport.close()
        startActivity(Intent(this,WebViewModeActivity::class.java))
    }

    private fun onClickMobileWebModePayment() {
        Iamport.close()
        startActivity(Intent(this,MobileWebViewModeActivity::class.java))
    }

    fun onClickCertification() {
        val userCode = "iamport"
        val certification = IamPortCertification(
            merchant_uid = getRandomMerchantUid(),
            company = "유어포트",
        )

        Iamport.certification(userCode, iamPortCertification = certification) { callBackListener.result(it) }
    }

    private fun registForegroundServiceReceiver(context: Context) {

        Iamport.enableChaiPollingForegroundService(enableService = true, enableFailStopButton = true)

        // 포그라운드 서비스 및 포그라운드 서비스 중지 버튼 클릭시 전달받는 broadcast 리시버
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            context.registerReceiver(receiver, IntentFilter().apply {
                addAction(CONST.BROADCAST_FOREGROUND_SERVICE)
                addAction(CONST.BROADCAST_FOREGROUND_SERVICE_STOP)
            }, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter().apply {
                addAction(CONST.BROADCAST_FOREGROUND_SERVICE)
                addAction(CONST.BROADCAST_FOREGROUND_SERVICE_STOP)
            })
        }

    }

    private val callBackListener = object : ICallbackPaymentResult {
        override fun result(iamPortResponse: IamPortResponse?) {
            val resJson = GsonBuilder().setPrettyPrinting().create().toJson(iamPortResponse)
            Log.i("SAMPLE", "결제 결과 콜백\n$resJson")
            Log.d("minhien", "$iamPortResponse  ")
            result66 = iamPortResponse
            if (iamPortResponse != null) {
                startActivity(Intent(this@HomeActivity,PaymentResultActivity::class.java))
                viewModel.resultCallback.postValue(Event(iamPortResponse))
            }
        }
    }

    private val pgSelectListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.pg = PG.values()[position]
            pgMethod.adapter = ArrayAdapter(
                this@HomeActivity, android.R.layout.simple_spinner_dropdown_item,
                Util.convertPayMethodNames(PG.values()[position])
            )

            pgMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.payMethod = Util.getMappingPayMethod(viewModel.pg).elementAt(pgMethod.selectedItemPosition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }


    private fun getUserInfo(accessToken: String?, callback: (User?) -> Unit) {
        val apiUrl = "https://openapi.naver.com/v1/nid/me"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Request.Method.GET, apiUrl,
            Response.Listener { response ->

                try {
                    val jsonObject = JSONObject(response)
                    val responseObj = jsonObject.getJSONObject("response")

                    val id = responseObj.getString("id")
                    val email = responseObj.getString("email")
                    val phone = responseObj.getString("mobile")
                    val username = responseObj.getString("name")

                    val user = User(id = 2, email = email, phone = phone, username = username, lat = 999.666, lng = 666.999, password = "666", inventoryQuantity = "100box")
                    Log.d("NaverUserInfo", id)

                    // Call the callback with the user object
                    callback(user)
                } catch (e: JSONException) {
                    Log.e("NaverUserInfoError", "Error parsing JSON: ${e.message}")
                    callback(null)
                }
            },
            Response.ErrorListener { error ->
                Log.e("NaverUserInfoError", "Error fetching user info: ${error.message}")
                callback(null)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $accessToken"
                return headers
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun updateUser(id : Int,user: User) {
        val call = ApiClient.apiService.updateUser(id, user)
        call.enqueue(object : Callback<User> {
            override fun onResponse(p0: Call<User>, p1: retrofit2.Response<User>) {
                if(p1.isSuccessful){
                    val data = p1.body()
                    Log.d("datacallback", "onResponse: $data")
                }
            }

            override fun onFailure(p0: Call<User>, p1: Throwable) {
                Log.d("datacallback", "onResponse: ${p1.message}")
            }

        })
    }

    private fun createnewuser(user: User){
        val createUser = ApiClient.apiService.createUser(user)
        createUser.enqueue(object : Callback<User>{
            override fun onResponse(p0: Call<User>, p1: retrofit2.Response<User>) {
                if(p1.isSuccessful){
                    val data = p1.body()
                    Log.d("datacallback", "onResponse: $data")
                }
            }

            override fun onFailure(p0: Call<User>, p1: Throwable) {
                Log.d("datacallback", "onResponse: ${p1.message}")
            }

        })
    }
    
    private fun loginuser(){
        val user = User(id = null, username = "hiendzvcl1", email = "hiendz1@example.com",lat = 9999.9999,lng = 999999.99, phone = "666" , password = "666", inventoryQuantity = "100")

        val call = ApiClient.apiService.loginUser(user)
        call.enqueue(object : Callback<String> {
            override fun onResponse(p0: Call<String>, p1: retrofit2.Response<String>) {
                val message = p1.body() // Response message
                Log.d("hien", "success8: ${message}")
            }

            override fun onFailure(p0: Call<String>, p1: Throwable) {
                Log.d("hien", "success9: , $p1")
            }

        })
    }
    
    private fun getAlluserInfo(){
        //get user info from naver login
        accessToken = NaverIdLoginSDK.getAccessToken().toString()

        //get all user info
        val getallUser = ApiClient.apiService.getAllUsers()
        getallUser.enqueue(object : Callback<List<User>>{
            override fun onResponse(p0: Call<List<User>>, p1: retrofit2.Response<List<User>>) {
                if(p1.isSuccessful){
                    val userList = p1.body()
                    userList?.forEach { user ->
                        Log.d("MainActivity", "User: ${user.username}")
                    }
                }
            }

            override fun onFailure(p0: Call<List<User>>, p1: Throwable) {
                Log.d("MainActivity", "User: ${p1}")
            }

        })
    }

    private fun getRandomMerchantUid(): String {
        return "muid_aos_${Date().time}"
    }
}