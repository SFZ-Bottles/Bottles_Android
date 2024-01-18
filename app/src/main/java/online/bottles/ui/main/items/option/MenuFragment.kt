package online.bottles.ui.main.items.option

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import online.bottles.api.response.bottlesUrl.bottlesLogout
import online.bottles.databinding.FragmentSettingsBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.login.LoginActivity
import online.bottles.ui.main.MainActivity
import org.json.JSONObject

class MenuFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var position: Int? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.menuOnResume()
        position = (activity as? MainActivity)?.getCurrentViewPagerPosition()
    }

    override fun onStop() {
        val currentPosition = position ?: 4
        when(currentPosition){
            0 ->{(activity as? MainActivity)?.homeFragmentOnResume() }
            1 ->{(activity as? MainActivity)?.searchFragmentOnResume()}
            2 ->{(activity as? MainActivity)?.messageFragmentOnResume()}
            3 ->{(activity as? MainActivity)?.myPageFragmentOnResume()}
            4 ->{ (activity as? MainActivity)?.otherUserPageFragmentOnResume()}
        }
        sildeOutAnime()
        (activity as? MainActivity)?.closeMenuFragment()
        super.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as? MainActivity)?.moveViewPager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editInfo.setOnClickListener(){
            val settingUserPreface = userPrefaceSettingFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, settingUserPreface)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        //뒤로가기 설정
        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.logout.setOnClickListener(){
            showLogoutDialog()
        }
    }
    private fun sildeOutAnime(){
        val slideOutAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        view?.startAnimation(slideOutAnimation)

        // 애니메이션 리스너 등록
        slideOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // 애니메이션 시작 시 수행할 작업
            }
            override fun onAnimationEnd(animation: Animation?) {
                // 애니메이션이 종료되면 뒤로가기 동작 수행
            }
            override fun onAnimationRepeat(animation: Animation?) {
                // 애니메이션 반복 시 수행할 작업
            }
        })
    }

    private fun showDialog(message : String){
        val builder = AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("확인",DialogInterface.OnClickListener(){
                    dialog, id -> dialog.dismiss()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun showLogoutDialog() {
        //로그아웃 다이얼로그
        val builder = AlertDialog.Builder(requireContext())
            .setMessage("정말 로그아웃하시겠습니까?")
            .setNegativeButton("취소",DialogInterface.OnClickListener(){dialog, id -> dialog.cancel()})
            .setPositiveButton("확인", DialogInterface.OnClickListener(){ dialog, id -> getLogout() })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun getLogout(){
        val jsonRequest:RequestBody = FormBody.Builder()
            .build()
        CoroutineScope(Dispatchers.Main).launch {
            val response = CoroutineScope(Dispatchers.Default).async {
                logout(jsonRequest)
            }.await()
            if(response != "ok, logout"){
                showDialog("로그 아웃 실패. 다시 시도해 주세요.")
            }else{
                val intent = Intent(requireActivity(),LoginActivity::class.java)
                val activity = requireActivity()
                startActivity(intent)
                activity.finish()
            }
        }
    }
    private suspend fun logout(jsonRequestBody: RequestBody): String {
        //client
        val client = OkHttpClient.Builder().build()
        //요청

        val request = Request.Builder()
            .url(bottlesLogout)
            .post(jsonRequestBody)
            .build()


       return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if(response.isSuccessful){
                    val responseBody = response.body?.string()
                    if(responseBody!=null) {
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.getString("message")

                        if(message == "ok, logout"){
                            return@withContext message
                        }else{
                            showDialog("로그 아웃에 실패하였습니다. 다시 시도해 주세요")
                        }
                    } else {
                        showDialog("서버 응답 오류 다시 시도해 주세요")
                    }
                } else {
                    showDialog("서버 응답 오류")
                }
            }catch (e:Exception){
                e.printStackTrace()
                showDialog("네트워크 응답 오류")
            }
            ""
        }.toString()

    }

}