package online.bottles.ui.main.items.follow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.R
import online.bottles.databinding.ActivityFollowerBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import online.bottles.ui.main.items.OtherUserPageFragment

class FollowerFragment : BaseFragment() {
    private lateinit var binding: ActivityFollowerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityFollowerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.followFragmentOnResume()
    }
    override fun onStop() {
        (activity as? MainActivity)?.myPageFragmentOnResume()
        super.onStop()


    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as? MainActivity)?.moveViewPager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val pageInclude = binding.pageInclude

        pageInclude.deleteUser.setOnClickListener{
            requireActivity().onBackPressed()
        }

        pageInclude.userProfile.setOnClickListener{
            val otherUserPage = OtherUserPageFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, otherUserPage)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        val backButton = binding.backButton
        backButton.setOnClickListener {
            // 뒤로가기 동작 수행
            requireActivity().onBackPressed()
        }

    }

}

