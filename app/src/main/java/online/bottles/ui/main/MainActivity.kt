package online.bottles.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import online.bottles.R
import online.bottles.databinding.ActivityMainBinding
import online.bottles.ui.base.BaseActivity
import online.bottles.ui.main.items.*
import online.bottles.ui.main.items.option.OptionFragment

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    private var currentViewPagerPosition = 0

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

                when(position){
                    0,1,2 ->{  menuOnlyVisible()
                    }
                    3 ->{  menuAndPlus()}
                }

            }
        })

        //메뉴 버튼 누르는 동작
        binding.bottlesMenuButton.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.slide_in_left,  // 드로어가 나타날 때 사용할 애니메이션
                R.anim.slide_out_left   // 드로어가 사라질 때 사용할 애니메이션
            )
            transaction.replace(R.id.page, OptionFragment())
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
    //뷰 페이저 포지션 값을 프레그먼트에 리턴해서 내가 어떤 뷰 페이저 위치에서 생성되었는지 알 수 있게 해줌
    fun getCurrentViewPagerPosition(): Int {
        return currentViewPagerPosition
    }

    //홈 페이지로 이동 시 동작
    fun homeFragmentOnResume(){
        moveViewPager()
        menuOnlyVisible()
    }
    //서치 페이지로 이동 시 동작
    fun searchFragmentOnResume(){
        moveViewPager()
        menuOnlyVisible()
    }
    //메시지 페이지로 이동 시 동작
    fun messageFragmentOnResume(){
        moveViewPager()
        menuOnlyVisible()
    }
    //내 페이지가 실행될 때 동작
    fun myPageFragmentOnResume(){
        moveViewPager()
        menuAndPlus()
    }

    //메뉴를 눌렀을 때 동작
    fun menuOnResume(){
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = false
        menuOnlyVisible()
    }
    //otherUserPageFragment 이동 시 동작
    fun otherUserPageFragmentOnResume(){
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = false
        dmAddMenu()
    }

    //팔로잉 팔로워가 실행될 때 동작
    fun followFragmentOnResume(){
        val mainViewPager = binding.mainViewPager
        val dmButton = binding.directMessage
        val addButton = binding.addUser
        val plus = binding.bottlesPlusButton
        mainViewPager.isUserInputEnabled = false

        dmButton.visibility = View.GONE
        addButton.visibility = View.GONE
        plus.visibility = View.INVISIBLE
    }

    //메뉴 아이콘만 보이게 하는 가시성
    fun menuOnlyVisible(){
        val dm = binding.directMessage
        val add = binding.addUser
        val pl = binding.bottlesPlusButton

        pl.visibility = View.INVISIBLE
        dm.visibility = View.GONE
        add.visibility = View.GONE
    }
    //메뉴와 plus 버튼을 보이게 하는 가시성
    fun menuAndPlus(){
        val dm = binding.directMessage
        val add = binding.addUser
        val pl = binding.bottlesPlusButton
        pl.visibility = View.VISIBLE
        dm.visibility = View.GONE
        add.visibility = View.GONE
    }
    //dm, add, 메뉴 버튼을 보이게 하는 가시성
    fun dmAddMenu(){
        val dm = binding.directMessage
        val add = binding.addUser
        val pl = binding.bottlesPlusButton

        pl.visibility = View.GONE
        dm.visibility = View.VISIBLE
        add.visibility = View.VISIBLE
    }
    fun moveViewPager(){
        val mainViewPager = binding.mainViewPager
        mainViewPager.isUserInputEnabled = true
    }
}
