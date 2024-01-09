package online.bottles.ui.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.*
import online.bottles.api.response.bottlesUrl
import online.bottles.databinding.ActivityLoginBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.ui.register.RegisterActivity
import online.bottles.api.response.loginResponse
import online.bottles.api.response.loginJWT
import online.bottles.ui.main.MainActivity

class LoginActivity : BaseActivity() {
    lateinit var binding: ActivityLoginBinding
    private val bottlesURL = bottlesUrl.bottlesLogin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signIn.setOnClickListener() {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener() {
            login()
        }
        binding.findIdButton.setOnClickListener() {}
        // binding.findPassword.setOnClickListener(){}


    }

    fun login() {
        var id = binding.editEmail.text.toString()
        var pw = binding.editPassword.text.toString()
        if (id.isEmpty()) {
            showDialog("Email을 입력해주세요")
        } else if (pw.isEmpty()) {
            showDialog("비밀 번호를 입력해주세요")
        } else {
            val jsonRequestBody: RequestBody = FormBody.Builder()
                .add("id", binding.editEmail.text.toString())
                .add("pw", binding.editPassword.text.toString())
                .build()

            CoroutineScope(Dispatchers.Main).launch {
                val response = CoroutineScope(Dispatchers.Default).async {
                    getLogin(jsonRequestBody)
                }.await()
                //메인 스레드 network 수행하면 안됨

            }
        }


    }

    suspend fun getLogin(jsonRequestBody: RequestBody): Unit? {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url(bottlesURL)
            .post(jsonRequestBody)
            .build()

        val gson = Gson()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    gson.fromJson(responseBody, loginJWT::class.java)?.let { token ->
                        Log.d("AuthToken", "Token: ${token.token}")
                        val sharedPreferences =
                            getSharedPreferences("jwt_token", Context.MODE_PRIVATE)
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
                            val errorData = gson.fromJson(errorJson, loginResponse::class.java)
                            if (errorData.error == "Invalid id") {
                                showDialog("아이디가 존재하지 않습니다.")
                                binding.emailErrorMessage.visibility = View.VISIBLE
                            } else {
                                showDialog("아이디와 비밀번호가 일치하지 않습니다.")
                                binding.passwordErrorMessage.visibility = View.VISIBLE
                            }
                        }
                        else -> {
                            // 기타 HTTP 에러 처리
                            showDialog("서버 오류가 발생하였습니다.")
                        }
                    }
                }
            } catch (e: Exception) {
                // 예외 처리
                e.printStackTrace()
                showDialog("네트워크 오류가 발생하였습니다.")
            }
        }
    }

    fun showDialog(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dialog = AlertDialog.Builder(this@LoginActivity)
                .setMessage(message)
                .setPositiveButton("확인") { _, _ -> }
                .create()
            dialog.show()
        }
    }
}

