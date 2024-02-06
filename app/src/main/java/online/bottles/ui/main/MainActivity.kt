package online.bottles.ui.main

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import online.bottles.R
import online.bottles.api.response.bottlesUrl
import online.bottles.databinding.ActivityMainBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.ui.login.LoginActivity
import online.bottles.ui.main.items.*
import online.bottles.ui.main.items.create.CreateAnAlbumFragment
import online.bottles.ui.main.items.home.HomeFragment
import online.bottles.ui.main.items.message.MessageFragment
import online.bottles.ui.main.items.option.MenuFragment
import online.bottles.ui.main.items.search.SearchFragment
import org.json.JSONObject

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding
    private var bottlesURL =bottlesUrl.bottlesValid
    private var currentViewPagerPosition = 0
    private var menuFragmentEnable = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("jwt_token", MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)
        if(token != null){
            getValidity(token)
        }





        // BottomNavigationView 스타일 설정
        val bottomNavigationView = binding.bottomNavigationView
        //bottomNavigationView.inflateMenu(R.menu.bottom_navigation_view)
        bottomNavigationView.itemIconTintList = null


        val mainViewPager = binding.mainViewPager
        mainViewPager.adapter = MainPagerAdapter(this)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuHomeButton -> {
                    // Home 버튼이 선택된 경우의 동작
                    mainViewPager.setCurrentItem(0, true)
                    true
                }
                R.id.menuSearchButton -> {
                    // Search 버튼이 선택된 경우의 동작
                    mainViewPager.setCurrentItem(1, true)
                    true
                }
                R.id.menuMessageButton -> {
                    // Message 버튼이 선택된 경우의 동작
                    mainViewPager.setCurrentItem(2, true)
                    true
                }
                R.id.menuUserButton -> {
                    // User 버튼이 선택된 경우의 동작
                    mainViewPager.setCurrentItem(3, true)
                    true
                }
                else -> false
            }
        }



        mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentViewPagerPosition = position //포지션 값 저장
                // 페이지가 선택될 때 스크롤 바 아이템의 위치를 변경
                val itemWidth = binding.scrItem1.width // 아이템의 너비
                // 스크롤 바 아이템 위치 계산
                val newX = position * itemWidth

                // 스크롤 바 아이템 이동
                binding.scrItem1.translationX = newX.toFloat()
                binding.scrItem2.translationX = newX.toFloat()
                binding.scrItem3.translationX = newX.toFloat()
                binding.scrItem4.translationX = newX.toFloat()
                // 페이지가 변경될 때 스와이프 동작을 제어

            }
        })

        //메뉴 버튼 누르는 동작
        binding.bottlesMenuButton.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            if (menuFragmentEnable) {
                menuFragmentEnable = false
                transaction.setCustomAnimations(
                    R.anim.slide_in_left,  // 드로어가 나타날 때 사용할 애니메이션
                    R.anim.slide_out_left   // 드로어가 사라질 때 사용할 애니메이션
                )
                transaction.replace(R.id.page, MenuFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
        //myPage에서 plus 버튼 누르는 동작
        binding.bottlesPlusButton.setOnClickListener() {
            val plusAlbum = CreateAnAlbumFragment()
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.page, plusAlbum)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    //뷰 페이저
    class MainPagerAdapter(fragmentManager: MainActivity) :
        FragmentStateAdapter(fragmentManager) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> SearchFragment()
                2 -> MessageFragment()
                3 -> MyPageFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    private fun getValidity(token: String) {

        val jsonRequestBody:RequestBody = FormBody.Builder()
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


    fun closeMenuFragment() {
        menuFragmentEnable = true
    }

    //뷰 페이저 포지션 값을 프레그먼트에 리턴해서 내가 어떤 뷰 페이저 위치에서 생성되었는지 알 수 있게 해줌
    fun getCurrentViewPagerPosition(): Int {
        return currentViewPagerPosition
    }

    //홈 페이지로 이동 시 동작
    fun homeFragmentOnResume() {
        moveViewPager()
        bottlesVisible(1)
    }

    //서치 페이지로 이동 시 동작
    fun searchFragmentOnResume() {
        moveViewPager()
        bottlesVisible(1)

    }

    //메시지 페이지로 이동 시 동작
    fun messageFragmentOnResume() {
        moveViewPager()
        bottlesVisible(1)
    }

    //내 페이지가 실행될 때 동작
    fun myPageFragmentOnResume() {
        moveViewPager()
        bottlesVisible(2)
    }

    // 앨범 추가 버튼 눌렀을 때 동작
    fun createAnAlbumFragmentOnResume() {
        stopViewPager()
        bottlesVisible(4)
    }

    //메뉴를 눌렀을 때 동작
    fun menuOnResume() {
        stopViewPager()
        bottlesVisible(5)
    }

    //otherUserPageFragment 이동 시 동작
    fun otherUserPageFragmentOnResume() {
        stopViewPager()
        bottlesVisible(3)
    }

    //팔로잉 팔로워가 실행될 때 동작
    fun followFragmentOnResume() {
        stopViewPager()
        bottlesVisible(5)
    }

    //뷰 페이저 잠금
    fun stopViewPager() {
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = false
    }

    //뷰 페이저 잠금 해제
    fun moveViewPager() {
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = true
    }

    private fun bottlesVisible(value: Int) {
        val bottomNav = binding.bottomNavigationFrame
        val dm = binding.directMessage
        val add = binding.addUser
        val pl = binding.bottlesPlusButton
        val menu = binding.bottlesMenuButton
        val viewBottom = binding.mainViewPager
        val bottomOn = 82
        when (value) {
            //메뉴 아이콘과 바텀 네비게이션 바를 보이게 하는 가시성
            1 -> {
                viewBottom.marginBottom
                bottomNav.visibility = View.VISIBLE
                menu.visibility = View.VISIBLE
                pl.visibility = View.INVISIBLE
                dm.visibility = View.GONE
                add.visibility = View.GONE
            }
            //바텀, 메뉴와 plus 버튼을 보이게 하는 가시성
            2 -> {
                bottomNav.visibility = View.VISIBLE
                menu.visibility = View.VISIBLE
                pl.visibility = View.VISIBLE
                dm.visibility = View.GONE
                add.visibility = View.GONE
            }
            //바텀,dm, add, 메뉴 버튼을 보이게 하는 가시성
            3 -> {
                bottomNav.visibility = View.VISIBLE
                menu.visibility = View.VISIBLE
                pl.visibility = View.GONE
                dm.visibility = View.VISIBLE
                add.visibility = View.VISIBLE
            }
            //모두 보이지 않게 하는 가시성
            4 -> {
                bottomNav.visibility = View.GONE
                menu.visibility = View.GONE
                pl.visibility = View.GONE
                dm.visibility = View.GONE
                add.visibility = View.GONE
            }
            //메뉴 아이콘만 보이게 하는 가시성
            5 -> {
                bottomNav.visibility = View.GONE
                menu.visibility = View.VISIBLE
                pl.visibility = View.INVISIBLE
                dm.visibility = View.GONE
                add.visibility = View.GONE
            }
        }

    }

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                dialog.dismiss()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
    fun showSessionExpiredDialog(){
        val builder = AlertDialog.Builder(this)
            .setMessage("세션이 만료되어 로그인 페이지로 이동합니다..")
            .setPositiveButton("확인",DialogInterface.OnClickListener(){dialog,id ->
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            })
        val alertDialog = builder.create()
        alertDialog.show()
    }
}

