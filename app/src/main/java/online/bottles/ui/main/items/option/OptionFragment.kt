package online.bottles.ui.main.items.option

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import online.bottles.R
import online.bottles.databinding.ActivitySettingsBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

class OptionFragment : BaseFragment() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivitySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.menuOnResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? MainActivity)?.moveViewPager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backButton
        backButton.setOnClickListener {
            // 슬라이드 아웃 애니메이션 적용
            val slideOutAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
            view.startAnimation(slideOutAnimation)

            // 애니메이션 리스너 등록
            slideOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // 애니메이션 시작 시 수행할 작업
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // 애니메이션이 종료되면 뒤로가기 동작 수행
                    (activity as? MainActivity)?.moveViewPager()
                    requireActivity().onBackPressed()
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    // 애니메이션 반복 시 수행할 작업
                }
            })
        }
    }
}