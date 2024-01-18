package online.bottles.ui.main.items.option

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.databinding.FragmentSettingUserPrefaceBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

class userPrefaceSettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingUserPrefaceBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingUserPrefaceBinding.inflate(inflater, container, false)
        return binding.root

    }



    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.menuOnResume()
    }

}