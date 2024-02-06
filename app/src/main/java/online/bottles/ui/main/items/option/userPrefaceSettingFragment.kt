package online.bottles.ui.main.items.option

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.bottles.R
import online.bottles.api.response.bottlesUrl.bottlesURL
import online.bottles.databinding.FragmentSettingUserPrefaceBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class userPrefaceSettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingUserPrefaceBinding
    private val REQ_GALLERY = 1001
    private val REQ_STORAGE_PERMISSION = 1002
    private var profUserImageUri : Uri? = null
    private lateinit var getUserInterfaceApi : GetUserProfile
    private lateinit var authToken: String
    private lateinit var userPassword : String
    private lateinit var userId : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userId = getSharedUserInterface().orEmpty()
        binding = FragmentSettingUserPrefaceBinding.inflate(inflater, container, false)
        authToken = getAuthToken()
        /*if(authToken!=null){
            getValidity(authToken)
        }*/
        return binding.root
    }


    private fun getSharedUserInterface(): String? {
        val sharedPreferencesForId = activity?.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferencesForId?.getString("userId", null).orEmpty()

    }
    private fun getAuthToken(): String {
        // Fragment에 연결된 Activity를 통해 SharedPreferences를 가져오도록
        val sharedPreferences = activity?.getSharedPreferences("jwt_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences?.getString("jwt_token",null).orEmpty()
        return authToken
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRetrofit()
        requestPermissionAgain()
        getProfile()
        binding.profileImageEdit.setOnClickListener(){
            //profile image setUp
            checkAndOpenGallery()
        }
        binding.profileImageDelete.setOnClickListener(){
            //기본 이미지로 변경
            showProfDeleteDialog()
        }
        binding.profileIdEdit.setOnClickListener {
            // ID 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("id",30)

        }
        binding.profilePasswordEdit.setOnClickListener {
            // 비밀번호 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("password",30)
        }
        binding.profileEmailEdit.setOnClickListener {
            // 이메일 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("email",40)
        }
        binding.profileUserNameInfo.setOnClickListener {
            // 사용자 이름 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("name",30)
        }
        binding.profileInfoEdit.setOnClickListener {
            // 사용자 정보 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("info",450)
        }
        binding.completeButton.setOnClickListener {
            // 회원 정보 수정을 서버에 요청하는 로직을 여기에 추가
            showSetUserDialog()
        }
    }


    //권한 요청 작업
    private fun checkAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 있으면 갤러리 열기
            openGallery()
        } else {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
        }
    }
    //갤러리 열기
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, REQ_GALLERY)
    }
    //권한 요청
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_STORAGE_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // 권한이 승인된 경우 갤러리 열기
                    openGallery()
                } else {
                    showDialog("권한이 필요합니다.")
                    requestPermissionAgain()
                }
            }
        }
    }

    //권한 요청 다시 하기
    private fun requestPermissionAgain() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_GALLERY && resultCode == Activity.RESULT_OK){
            val selectedImageUri = data?.data
            if(selectedImageUri != null){
                binding.profUserImage.setImageURI(selectedImageUri)
                profUserImageUri = selectedImageUri
            }
        }
    }

    override fun onResume() { super.onResume()
        (activity as? MainActivity)?.menuOnResume()
    }
    //회원 정보 반환하기
    private fun getProfile(){
        val userid = userId
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = getUserInterfaceApi.userProfile(userid,authToken)
                if (response.isSuccessful) {
                    // 응답 성공
                    val userProfile = response.body()
                    withContext(Dispatchers.Main) {
                        // UI 업데이트
                        binding.userId.text = userProfile?.id
                        binding.userName.text = userProfile?.name
                        binding.userEmail.text = userProfile?.email
                        binding.userInfo.text = userProfile?.info
                        userProfile?.avatar?.let { avatarUrl ->
                            Glide.with(this@userPrefaceSettingFragment)
                                .load(avatarUrl)
                                .into(binding.profUserImage) // 여기에 ImageView ID를 넣어주세요.
                        }

                    }
                } else {
                    // 에러 처리
                    Log.e("UserProfileError", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 오류 처리
                Log.e("NetworkError", "Error: ${e.message}")
            }
        }
    }
    private fun showProfDeleteDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("등록한 이미지를 기본 이미지로 변경하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                // 사용자가 확인 버튼을 누르면 앱 종료
                val defaultImage = R.drawable.testduck
                binding.profUserImage.setImageResource(defaultImage)
                profUserImageUri = null
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                // 사용자가 취소 버튼을 누르면 다이얼로그 닫기
                dialog.dismiss()
            })

        val alertDialog = builder.create()
        alertDialog.show()
    }
/*
    private fun getValidity(token: String) {

        val jsonRequestBody: RequestBody = FormBody.Builder()
            .add("token","Plz refer to header")
            .build()
        CoroutineScope(Dispatchers.Main).launch {
            val response = CoroutineScope(Dispatchers.Default).async {
                checkValidity(token,jsonRequestBody)
            }.await()
            if(response != "valid token"){
                showSessionExpiredDialog()
            }else{
            }
        }
    }
    private suspend fun checkValidity(token: String, jsonRequestBody: RequestBody): String {
        //client
        val client = OkHttpClient.Builder().build()
        //요청

        val request = Request.Builder()
            .url(bottlesURL)
            .post(jsonRequestBody)
            .header("Authorization",token)
            .build()


        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if(response.isSuccessful){
                    val responseBody = response.body?.string()
                    if(responseBody!=null){
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.getString("message")

                        return@withContext message
                    } else {
                        showDialog("서버 응답 오류 다시 시도해 주세요")
                    }
                } else {
                    showDialog("서버 응답 오류")
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }.toString()

    }
*/
    private fun setUserProfile() {
        val newUserId = binding.userId.text.toString()
        val newUserPassword = userPassword
        val newUserName = binding.userName.text.toString()
        val newUserEmail = binding.userEmail.text.toString()
        val newUserInFo = binding.userInfo.text.toString()
        val newUserAvatar = profUserImageUri
        val isPasswordChanged = newUserPassword != null && newUserPassword.isNotEmpty()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (isPasswordChanged) {
                    getUserInterfaceApi.setUserProfile(
                        userId,
                        authToken,
                        newUserId,
                        newUserPassword,
                        newUserName,
                        newUserEmail,
                        newUserInFo,
                        newUserAvatar
                    )
                } else {
                    getUserInterfaceApi.setUserProfileWithoutPassword(
                    userId,
                    authToken,
                    newUserId,
                    newUserName,
                    newUserEmail,
                    newUserInFo,
                    newUserAvatar
                    )
                }
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    val newToken = userProfile?.newToken
                    withContext(Dispatchers.Main) {
                        showDialog("성공적으로 변경되었습니다.")
                        val sharedPreferenceToToken =
                        requireActivity().getSharedPreferences("jwt_token", Context.MODE_PRIVATE)
                        sharedPreferenceToToken.edit().putString("jwt_token", newToken).apply()
                        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                        //이전의 동작으로 쌓인 모든 stack을 지우고 새로 시작한 것처럼  mainActivity에서 다시 시작
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        requireActivity().finish()
                    }
            }else{
                    Log.e("UserProfileUpdateError",
                        "Failed to update user profile. Response code: ${response.code()}, Message: ${response.message()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 네트워크 오류 처리
            Log.e("NetworkError", "Error: ${e.message}")
        }

    }
}

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(message)
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                dialog.dismiss()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    /*fun showSessionExpiredDialog(){
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage("세션이 만료되어 로그인 페이지로 이동합니다..")
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }*/
    //값 수정
    private fun showEditDialog(target :String,maxInputLength:Int){
        val builder = AlertDialog.Builder(requireActivity())
        //빌더에 사용될 레이아웃 가져옴
        val minInputLength = 2
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_for_edit_profile,null)
        //editText 찾고
        val editTextNew = dialogView.findViewById<EditText>(R.id.editTextNew)
        if (target == "password") {
            editTextNew.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            // EditText에 입력되는 텍스트를 가리기 위해 설정
        }

        editTextNew.filters = arrayOf(android.text.InputFilter.LengthFilter(maxInputLength))
        //빌더에 다이얼로그 레이아웃 설정
        builder.setView(dialogView)
            .setTitle("Please enter here")
            .setPositiveButton("확인"){dialog,which->
                val newId = editTextNew.text.toString()
                val resources = resources
                if(newId.length >= minInputLength){
                    when(target){
                        "id"->{binding.userId.text = newId}
                        "password"->{userPassword = newId}
                        "email"->{ binding.userEmail.text = newId}
                        "name"->{binding.userName.text = newId}
                        "info"->{ binding.userInfo.text = newId }
                    }
                }else{
                    Toast.makeText(requireActivity(), "$target 은 반드시 $minInputLength 글자 이상 이어야 합니다", Toast.LENGTH_SHORT).show()
                    showEditDialog(target, maxInputLength)
                }

            }
            .setNegativeButton("취소"){dialog,which->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

    }
    private fun showSetUserDialog(){
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage("정말 수정하시겠습니까?")
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                setUserProfile()
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener(){dialog,id->
                dialog.dismiss()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun initRetrofit(){
        val retrofit = Retrofit.Builder()
        .baseUrl(bottlesURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        getUserInterfaceApi = retrofit.create(GetUserProfile::class.java)

    }

}