package online.bottles.ui.register
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import online.bottles.api.response.loginResponse
import online.bottles.databinding.ActivitySignupBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.api.response.registerResponse
import online.bottles.ui.login.LoginActivity


class RegisterActivity: BaseActivity() {
    lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener() {
            coroutine()
        }


    }


    fun coroutine() {
        //데이터를 담아 보낼 바디를 만든다

        val jsonRequestBody: RequestBody = FormBody.Builder()
            .add("id", binding.signId.text.toString())
            .add("pw", binding.signPassword.text.toString())
            .add("name", binding.signUserName.text.toString())
            .add("email", binding.signEmail.text.toString())
            .add("info", binding.signPreface.text.toString())
            .build()
        //코루틴스코프 동기실행(main)
        CoroutineScope(Dispatchers.Main).launch {
            //코루틴스코프 비동기실행(default//백그라운드 실행은 디폴트로)
            val response = CoroutineScope(Dispatchers.Default).async {
                //network
                getSignUp(jsonRequestBody)
            }.await()//어사인 비동기 방식이 끝나면 await()로 특정 동작을 실행해 주어야 함
            //메인스레드(network 동작을 하면 안되는 구역)

            showDialog("성공적으로 가입되었습니다.")
        }
    }

    suspend fun getSignUp(jsonRequestBody: RequestBody): registerResponse? {
        //클라이언트만들기
        val client = OkHttpClient.Builder().build()
        //요청
        val request = Request.Builder()
            .url("http:/14.4.145.80:8000/api/users/")
            .post(jsonRequestBody)
            .build()
        val gson = Gson()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    showDialog("성공적으로 가입되셨습니다!")
                }
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                gson.fromJson(response.body?.string(), registerResponse::class.java)
            } else {
                // HTTP 에러 코드에 따라 다른 다이얼로그를 띄워줌
                when (response.code) {
                    409 -> {
                        val errorJson = response.body?.string()
                        val errorData = gson.fromJson(errorJson, registerResponse::class.java)

                        if(errorData.error == "already exist id"){
                            showDialog("이미 가입된 아이디 입니다.")
                        }else if(errorData.error == "already exist email"){
                            showDialog("이미 가입된 이메일 입니다.")
                        }
                        else{
                            showDialog("작성되지 않은 부분 체크(고쳐야됨)")
                        }

                    }
                    else -> {
                        // 기타 HTTP 에러 처리
                        showDialog("서버 오류가 발생하였습니다.")
                    }
                }
                null
            }
        }
    }
    fun showDialog(message: String){
        val dialog = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ -> }
            .create()
        dialog.show()
    }
}
/*




        binding.signUpButton.setOnClickListener() {

                val jsonObject = JSONObject()

                try {
                    jsonObject.put("id", binding.signId.text)
                    jsonObject.put("pw", binding.signPassword.text)
                    jsonObject.put("name", binding.signUserName.text)
                    jsonObject.put("email", binding.signEmail.text)
                    jsonObject.put("preface", binding.signPreface.text)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            var str :String = jsonObject.toString()
            val url = URL("http://10.0.2.2:8000/")

            binding.termsOfServiceButton.text = jsonObject.toString()+"연결완료"

            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connect()

            val sendJson = DataOutputStream(conn.outputStream)
            sendJson.writeBytes(str)
            sendJson.flush()

        }            }

////////////////////////////////////////////////////////////
 fun coroutine(){
        val jsonObject = JSONObject()
        try{
            jsonObject.put("id",binding.signId.text)
            jsonObject.put("pw",binding.signId.text)
            jsonObject.put("name",binding.signId.text)
            jsonObject.put("email",binding.signId.text)
            jsonObject.put("preface",binding.signId.text)
        }catch(e:Exception){
            e.printStackTrace()
        }
        //코루틴스코프 동기실행(main)
        CoroutineScope(Dispatchers.Main).launch{
            //코루틴스코프 비동기실행(default//백그라운드 실행은 디폴트로)
            val http = CoroutineScope(Dispatchers.Default).async{
               //network

            }.await()//어사인 비동기 방식이 끝나면 await()로 특정 동작을 실행해 주어야 함
            //메인스레드
        }
    }
    fun getHttp(){
        //클라이언트 생성
        val client = OkHttpClient.Builder().build()//클라이언트를 만드는 okHttp
        //요청
        val request = Request.Builder().url("http://10.0.2.2:8000/").build()//okHttp 요청방식
        //요청 보내기.enqueue() 비동기 //.execute() 동기
        client.newCall(request).execute().use {

        }
    }

//////////////////////////////////////////////////////
class RegisterActivity: BaseActivity(){

    var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.signUpButton.setOnClickListener(){
            val parameters: MutableMap<String, String> = HashMap()
            try {
                parameters.put("id", binding.signId.text.toString())
                parameters.put("pw", binding.signPassword.text.toString())
                parameters.put("name", binding.signUserName.text.toString())
                parameters.put("email", binding.signEmail.text.toString())
                parameters.put("preface", binding.signPreface.text.toString())
            }catch (e:Exception){
                e.printStackTrace()
            }

            Thread {
                var jsonData = JSONObject(parameters as Map<*, *>?)
                var str :String = jsonData.toString()
                val url = URL("http://10.0.2.2:8000/")

                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connect()

                val sendJson = DataOutputStream(conn.outputStream)
                sendJson.writeBytes(str)
                sendJson.flush()
                sendJson.close()
                conn.disconnect()


            }
        }
    }

}






/////////////////////////////////////////////////////////////////////
class registerActivity : BaseActivity() {
    lateinit var binding: ActivitySignupBinding

    var volleyRequestQueue: RequestQueue? = null
    var dialog: ProgressDialog? = null
    var url = "10.0.2.2:8000"//127.0.0.1:8000
    var TAG = "Handy Opinon T"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fun SendSignUpData(
            id: String,
            pw: String,
            name: String,
            email: String,
            preface: String
        ) {

            volleyRequestQueue = Volley.newRequestQueue(this)
            dialog = ProgressDialog.show(this, "", "please wait..", true)
            val parameters: MutableMap<String, String> = HashMap()

            parameters.put("id", id)
            parameters.put("pw", pw)
            parameters.put("name", name)
            parameters.put("email", email)
            parameters.put("preface", preface)

            val strReq: StringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener {
                        response ->
                    Log.e(TAG, "response: " + response)
                    dialog?.dismiss()

                    // Handle Server response here
                    try {
                        val responseObj = JSONObject(response)
                        val isSuccess = responseObj.getBoolean("success")
                        val code = responseObj.getInt("code")
                        val message = responseObj.getString("message")

                        if (responseObj.has("data")) {

                            val data = responseObj.getJSONObject("data")
                            // Handle your server response data here

                        }
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    } catch (e: Exception) { // caught while parsing the response
                        Log.e(TAG, "problem occurred")
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { volleyError -> // error occurred
                    Log.e(TAG, "problem occurred, volley error: " + volleyError.message)
                }) {

                override fun getParams(): MutableMap<String, String> {
                    return parameters;
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {

                    val headers: MutableMap<String, String> = HashMap()
                    // Add your Header paramters here
                    return headers
                }
            }
            volleyRequestQueue?.add(strReq)
        }

        binding.signUpButton.setOnClickListener(){
            SendSignUpData(
                binding.signId.text.toString(),
                binding.signPassword.text.toString(),
                binding.signUserName.text.toString(),
                binding.signEmail.text.toString(),
                binding.signPreface.text.toString(),
            )
        }

    }

}
*/
