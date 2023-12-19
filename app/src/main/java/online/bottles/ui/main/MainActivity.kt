package online.bottles.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import online.bottles.R
import online.bottles.api.response.registerResponse
import online.bottles.databinding.ActivityMainBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.ui.login.LoginActivity
import online.bottles.ui.main.items.*
import online.bottles.ui.main.items.create.CreateAnAlbumFragment
import online.bottles.ui.main.items.home.HomeFragment
import online.bottles.ui.main.items.option.MenuFragment
import org.json.JSONObject

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding
    private var bottlesUrl = "http://14.4.145.80:8000/api/auth/validate-token/"
    private var currentViewPagerPosition = 0
    private var tokenValidity = true
    private var menuFragmentEnable = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BottomNavigationView 스타일 설정
        val bottomNavigationView = binding.bottomNavigationView
        //bottomNavigationView.inflateMenu(R.menu.bottom_navigation_view)
        bottomNavigationView.itemIconTintList = null


        val sharedPreferences = getSharedPreferences("jwt_token", MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)
        if (token != null) {
            checkTokenValidity(token)
        }else{
            val intent = Intent(this@MainActivity,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        if(!tokenValidity){
            val intent = Intent(this@MainActivity,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


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
    fun checkTokenValidity(token:String) {
        val jsonRequestBody:RequestBody = FormBody.Builder()
            .add("token",token)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            //코루틴스코프 비동기실행(default//백그라운드 실행은 디폴트로)
            val response = CoroutineScope(Dispatchers.Default).async {
                //network
                getValidity(jsonRequestBody)
            }.await()//어사인 비동기 방식이 끝나면 await()로 특정 동작을 실행해 주어야 함
            //메인스레드(network 동작을 하면 안되는 구역)


        }
    }

    suspend fun getValidity(jsonRequestBody: RequestBody) {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(bottlesUrl)
            .post(jsonRequestBody)
            .build()

        return withContext(Dispatchers.IO){
            val response = client.newCall(request).execute()
            if(response.isSuccessful){
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val message = jsonObject.getString("message")
                    if(message == "valid token"){
                        withContext(Dispatchers.Main){
                            tokenValidity = true
                        }
                    }
                }else{
                    tokenValidity = false
                }
            } else{
                withContext(Dispatchers.Main){
                    showDialog("네트워크 연결이 불안정합니다. 다시 시도해주세요.")
                }

            }
        }

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
        CoroutineScope(Dispatchers.Main).launch {
            val dialog = AlertDialog.Builder(this@MainActivity)
                .setMessage(message)
                .setPositiveButton("확인") { _, _ -> }
                .create()
            dialog.show()
        }
    }
}

