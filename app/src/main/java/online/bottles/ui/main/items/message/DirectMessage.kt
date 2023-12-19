package online.bottles.ui.main.items.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import online.bottles.R
import online.bottles.databinding.FragmentSendMessageBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

class DirectMessage :BaseFragment() {
    private lateinit var binding : FragmentSendMessageBinding
    private var textData: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener(){
            requireActivity().onBackPressed()
        }
        binding.sendButton.setOnClickListener(){
            var text = binding.sendMessage
            textData = text.text.toString()
            text.text.clear()
        }
    }

    override fun onStart() {

        super.onStart()
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.createAnAlbumFragmentOnResume()
    }
    override fun onStop() {
        sildeOutAnime()
        (activity as? MainActivity)?.messageFragmentOnResume()
        super.onStop()
    }
    //애니메이션
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
/**/