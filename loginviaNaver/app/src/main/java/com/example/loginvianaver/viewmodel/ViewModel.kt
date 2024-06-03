package com.example.loginvianaver.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.loginvianaver.modell.User
import com.iamport.sdk.data.sdk.*
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.Event
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ViewModel : ViewModel() {

    lateinit var pg: PG
    lateinit var payMethod: PayMethod
    var userCode: String = ""
    var paymentName: String = ""
    var merchantUid: String = ""
    var amount: String = ""
    var cardDirectCode: String = ""

    val resultCallback = MutableLiveData<Event<IamPortResponse>>()
    override fun onCleared() {
        Iamport.close()
        super.onCleared()
    }

    /**
     * SDK 에 결제 요청할 데이터 구성
     */
    fun createIamPortRequest(): IamPortRequest {
        val card = if (cardDirectCode.isNotEmpty()) Card(Direct(code = cardDirectCode)) else null

        return IamPortRequest(
            pg = pg.makePgRawName(pgId = ""),           // PG 사
            pay_method = payMethod.name,                // 결제수단
            name = paymentName,                         // 주문명
            merchant_uid = merchantUid,                 // 주문번호
            amount = amount,                            // 결제금액
            buyer_name = "남궁안녕",
            card = card, // 카드사 다이렉트
            custom_data = """
                {
                  "employees": {
                    "employee": [
                      {
                        "id": "1",
                        "firstName": "Tom",
                        "lastName": "Cruise",
                        "photo": "https://jsonformatter.org/img/tom-cruise.jpg",
                        "cuppingnote": "[\"일\",\"이\",\"삼\",\"사\",\"오\",\"육\",\"칠\"]"
                      },
                      {
                        "id": "2",
                        "firstName": "Maria",
                        "lastName": "Sharapova",
                        "photo": "https://jsonformatter.org/img/Maria-Sharapova.jpg"
                      },
                      {
                        "id": "3",
                        "firstName": "Robert",
                        "lastName": "Downey Jr.",
                        "photo": "https://jsonformatter.org/img/Robert-Downey-Jr.jpg"
                      }
                    ]
                  }
                }
            """.trimIndent()
//            customer_uid = getRandomCustomerUid() // 정기결제
        )
    }

    private fun getRandomCustomerUid(): String {
        return "mcuid_aos_${Date().time}"
    }

    fun getUserInfo(accessToken: String?,context : Context, callback: (User?) -> Unit) {
        val apiUrl = "https://openapi.naver.com/v1/nid/me"
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)

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


}