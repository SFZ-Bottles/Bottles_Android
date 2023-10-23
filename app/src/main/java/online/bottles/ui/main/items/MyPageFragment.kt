package online.bottles.ui.main.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.R
import online.bottles.databinding.FragmentPage4MyPageBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import online.bottles.ui.main.items.follow.FollowerFragment
import online.bottles.ui.main.items.follow.FollowingFragment

class MyPageFragment : BaseFragment() {

    private var _binding : FragmentPage4MyPageBinding?= null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage4MyPageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        (activity as? MainActivity)?.myPageFragmentOnResume()
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val follower = binding.followerText
        val following = binding.followingText
        val followerCount = binding.follower
        val followingCount = binding.following

        followerCount.setOnClickListener {
            val followerFragment = FollowerFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, followerFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        follower.setOnClickListener {
            val followerFragment = FollowerFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, followerFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        followingCount.setOnClickListener {
            val followingFragment = FollowingFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, followingFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        following.setOnClickListener {
            val followingFragment = FollowingFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, followingFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }


    }
}