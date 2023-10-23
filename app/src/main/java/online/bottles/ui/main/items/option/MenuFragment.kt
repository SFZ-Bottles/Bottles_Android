package online.bottles.ui.main.items.option

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import online.bottles.R
import online.bottles.databinding.FragmentSettingsBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

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

}