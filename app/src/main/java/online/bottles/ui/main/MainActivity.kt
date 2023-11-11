package online.bottles.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import online.bottles.R
import online.bottles.databinding.ActivityMainBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.ui.main.items.*
import online.bottles.ui.main.items.create.CreateAnAlbumFragment
import online.bottles.ui.main.items.option.MenuFragment

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    private var currentViewPagerPosition = 0
    private var menuFragmentEnable = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BottomNavigationView 스타일 설정
        val bottomNavigationView = binding.bottomNavigationView
        //bottomNavigationView.inflateMenu(R.menu.bottom_navigation_view)
        bottomNavigationView.itemIconTintList = null

        val token = null

        val sharedPreferences = getSharedPreferences("bottles_login_token", MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()

        val loadedToken = sharedPreferences.getString("jwt_token", null)
        /* if(token != null){
           binding.tk.text ="JWT Token: $token"
           binding.tk.visibility
       }*/
        // viewPager 설정
        val mainViewPager = binding.mainViewPager
        mainViewPager.adapter = MainPagerAdapter(this)




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
            if(menuFragmentEnable) {
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
        binding.bottlesPlusButton.setOnClickListener(){
            val plusAlbum = CreateAnAlbumFragment()
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.page, plusAlbum)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    class MainPagerAdapter(fragmentManager: MainActivity) : FragmentStateAdapter(fragmentManager) {
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
    fun closeMenuFragment(){
        menuFragmentEnable = true
    }
    //뷰 페이저 포지션 값을 프레그먼트에 리턴해서 내가 어떤 뷰 페이저 위치에서 생성되었는지 알 수 있게 해줌
    fun getCurrentViewPagerPosition(): Int {
        return currentViewPagerPosition
    }

    //홈 페이지로 이동 시 동작
    fun homeFragmentOnResume(){
        moveViewPager()
        bottlesVisible(1)
    }
    //서치 페이지로 이동 시 동작
    fun searchFragmentOnResume(){
        moveViewPager()
        bottlesVisible(1)

    }
    //메시지 페이지로 이동 시 동작
    fun messageFragmentOnResume(){
        moveViewPager()
        bottlesVisible(1)
    }
    //내 페이지가 실행될 때 동작
    fun myPageFragmentOnResume(){
        moveViewPager()
        bottlesVisible(2)
    }
    // 앨범 추가 버튼 눌렀을 때 동작
    fun createAnAlbumFragmentOnResume(){
        stopViewPager()
        bottlesVisible(4)
    }

    //메뉴를 눌렀을 때 동작
    fun menuOnResume(){
        stopViewPager()
        bottlesVisible(5)
    }
    //otherUserPageFragment 이동 시 동작
    fun otherUserPageFragmentOnResume(){
        stopViewPager()
        bottlesVisible(3)
    }

    //팔로잉 팔로워가 실행될 때 동작
    fun followFragmentOnResume(){
        stopViewPager()
        bottlesVisible(5)
    }
    //뷰 페이저 잠금
    fun stopViewPager(){
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = false
    }
    //뷰 페이저 잠금 해제
    fun moveViewPager(){
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = true
    }
    private fun bottlesVisible(value:Int){
        val bottomNav = binding.bottomNavigationFrame
        val dm = binding.directMessage
        val add = binding.addUser
        val pl = binding.bottlesPlusButton
        val menu = binding.bottlesMenuButton
        val viewBottom = binding.mainViewPager
        val bottomOn = 82
        when (value){
           //메뉴 아이콘과 바텀 네비게이션 바를 보이게 하는 가시성
            1 -> {  viewBottom.marginBottom
                    bottomNav.visibility = View.VISIBLE
                    menu.visibility = View.VISIBLE
                    pl.visibility = View.INVISIBLE
                    dm.visibility = View.GONE
                    add.visibility = View.GONE}
            //바텀, 메뉴와 plus 버튼을 보이게 하는 가시성
            2 -> {  bottomNav.visibility = View.VISIBLE
                    menu.visibility = View.VISIBLE
                    pl.visibility = View.VISIBLE
                    dm.visibility = View.GONE
                    add.visibility = View.GONE}
            //바텀,dm, add, 메뉴 버튼을 보이게 하는 가시성
            3 -> {  bottomNav.visibility = View.VISIBLE
                    menu.visibility = View.VISIBLE
                    pl.visibility = View.GONE
                    dm.visibility = View.VISIBLE
                    add.visibility = View.VISIBLE}
            //모두 보이지 않게 하는 가시성
            4 -> {  bottomNav.visibility = View.GONE
                    menu.visibility = View.GONE
                    pl.visibility = View.GONE
                    dm.visibility = View.GONE
                    add.visibility = View.GONE}
            //메뉴 아이콘만 보이게 하는 가시성
            5 -> {
                    bottomNav.visibility = View.GONE
                    menu.visibility = View.VISIBLE
                    pl.visibility = View.INVISIBLE
                    dm.visibility = View.GONE
                    add.visibility = View.GONE}
        }

    }
}
