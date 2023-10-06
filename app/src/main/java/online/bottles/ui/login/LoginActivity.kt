package online.bottles.ui.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import online.bottles.databinding.ActivityLoginBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.ui.register.RegisterActivity
import online.bottles.api.response.loginResponse
import online.bottles.api.response.loginJWT
import online.bottles.ui.main.MainActivity

class LoginActivity : BaseActivity(){
        lateinit var binding : ActivityLoginBinding

        override fun onCreate(savedInstanceState: Bundle?)
        {
            super.onCreate(savedInstanceState)

            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.signIn.setOnClickListener() {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            binding.loginButton.setOnClickListener(){
                login()
            }
            binding.findIdButton.setOnClickListener(){}
           // binding.findPassword.setOnClickListener(){}


        }

        fun login(){
            val jsonRequestBody : RequestBody = FormBody.Builder()
                .add("id",binding.editEmail.text.toString())
                .add("pw",binding.editPassword.text.toString())
                .build()

            CoroutineScope(Dispatchers.Main).launch {
                val response = CoroutineScope(Dispatchers.Default).async{
                    getLogin(jsonRequestBody)
                }.await()
                //메인 스레드 network 수행하면 안됨

            }


        }

        suspend fun getLogin(jsonRequestBody: RequestBody): Unit? {
            //클라이언트만들기
            val client = OkHttpClient.Builder().build()
            //요청
            val request = Request.Builder()
                .url("http://14.4.145.80:8000/api/auth/login/")
                .post(jsonRequestBody)
                .build()


            val gson = Gson()

            return withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    gson.fromJson(response.body?.string(), loginJWT::class.java)?.let { token ->
                       val sharedPreferences = getSharedPreferences("bottles_login_token",Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("jwt_token", token.token)
                        editor.apply()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }

                } else {
                    // HTTP 에러 코드에 따라 다른 다이얼로그를 띄워줌
                    when (response.code) {
                        401 -> {
                            val errorJson = response.body?.string()
                            val errorData = gson.fromJson(errorJson,loginResponse::class.java)
                            if(errorData.error == "Invalid id"){
                                showDialog("아이디가 존재하지 않습니다.")
                                binding.emailErrorMessage.visibility = View.VISIBLE
                            }
                            else{
                                showDialog("아이디와 비밀번호가 일치하지 않습니다.")
                                binding.passwordErrorMessage.visibility = View.VISIBLE
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

