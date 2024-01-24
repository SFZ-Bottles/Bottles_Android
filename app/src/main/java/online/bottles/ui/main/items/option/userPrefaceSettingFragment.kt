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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import online.bottles.R
import online.bottles.api.response.bottlesUrl.bottlesURL
import online.bottles.databinding.FragmentSettingUserPrefaceBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.login.LoginActivity
import online.bottles.ui.main.MainActivity
import org.json.JSONObject

class userPrefaceSettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingUserPrefaceBinding
    private val REQ_GALLERY = 1001
    private val REQ_STORAGE_PERMISSION = 1002
    private var profUserImageUri : Uri? = null
    private lateinit var authToken: String
    private lateinit var userPassword : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingUserPrefaceBinding.inflate(inflater, container, false)
        return binding.root
        checkPermissionAndRequest()
        authToken = getAuthToken()
        getValidity(authToken)
    }
    private fun getAuthToken(): String {
        // Fragment에 연결된 Activity를 통해 SharedPreferences를 가져오도록
        val sharedPreferences = activity?.getSharedPreferences("jwt_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences?.getString("jwt_token","token is null").orEmpty()
        Log.d("AuthToken", "Token: $authToken")
        return authToken
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileEmailEdit.setOnClickListener(){
            //profile image setUp
            checkPermissionAndRequest()
            checkAndOpenGallery()
        }
        binding.profileImageDelete.setOnClickListener(){
            //기본 이미지로 변경
            showProfDeleteDialog()
        }
        binding.profileIdEdit.setOnClickListener {
            // ID 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("id")

        }

        binding.profilePasswordEdit.setOnClickListener {
            // 비밀번호 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("password")
        }

        binding.profileEmailEdit.setOnClickListener {
            // 이메일 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("email")
        }

        binding.profileUserNameInfo.setOnClickListener {
            // 사용자 이름 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("name")
        }

        binding.profileInfoEdit.setOnClickListener {
            // 사용자 정보 수정 다이얼로그 또는 화면으로 이동하는 로직을 여기에 추가
            showEditDialog("info")
        }

        binding.completeButton.setOnClickListener {
            // 회원 정보 수정을 서버에 요청하는 로직을 여기에 추가
        }


    }


    //권한 요청 작업
    private fun checkPermissionAndRequest() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101 // 요청 코드 (임의로 선택 가능)
            )
        } else {
            // 권한이 이미 승인된 경우, 원하는 작업 수행
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            101 -> {
                // 외부 저장소 읽기 권한 요청 결과 처리
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 승인된 경우, 원하는 작업 수행
                } else {
                    // 권한이 거부된 경우, 사용자에게 알림 표시 또는 다른 조치 수행
                    Toast.makeText(requireContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show()

                    // 권한이 거부된 경우, 다시 권한을 요청하는 함수 호출
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
    // 겔러리 열기
    private fun checkAndOpenGallery() {
        var writePermission = ContextCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var readPermission = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED ||
            readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
        } else {
            // 권한이 있으면 갤러리 열기
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"
            startActivityForResult(intent, REQ_GALLERY)
        }
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
                showDialog("(확인)유효한 토큰")
            }
        }
    }
    suspend fun checkValidity(token: String,jsonRequestBody: RequestBody): String {
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

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(message)
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                dialog.dismiss()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    fun showSessionExpiredDialog(){
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage("세션이 만료되어 로그인 페이지로 이동합니다..")
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    //값 수정
    private fun showEditDialog(target :String){
        val builder = AlertDialog.Builder(requireActivity())
        //빌더에 사용될 레이아웃 가져옴
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_for_edit_profile,null)
        //editText 찾고
        val editTextNew = dialogView.findViewById<EditText>(R.id.editTextNew)
        //뷰 ID 매핑
        val viewIdToRequestMap = mapOf(
            "id" to R.string.userId,
            "email" to R.string.userEmail,
            "name" to R.string.userName,
            "info" to R.string.userInfo
        )
        //빌더에 다이얼로그 레이아웃 설정
        builder.setView(dialogView)
            .setTitle("Please enter here")
            .setPositiveButton("확인"){dialog,which->
                val newId = editTextNew.text.toString()
                val resources = resources
                val targetViewId = viewIdToRequestMap[target]
                when(target){
                    "id"->{
                        /*val targetTextView = dialogView.findViewById<TextView>(targetViewId)
                        targetTextView.text = newId*/
                        }
                    "password"->{userPassword = newId}
                    "email"->{ val resId = resources.getIdentifier("userEmail", "string", requireActivity().packageName)
                        resources.getString(resId) }
                    "name"->{val resId = resources.getIdentifier("userName", "string", requireActivity().packageName)
                        resources.getString(resId)}
                    "info"->{val resId = resources.getIdentifier("userInfo", "string", requireActivity().packageName)
                        resources.getString(resId)}
                }

            }
            .setNegativeButton("취소"){dialog,which->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

    }
}