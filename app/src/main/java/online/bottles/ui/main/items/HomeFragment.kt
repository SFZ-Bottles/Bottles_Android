package online.bottles.ui.main.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.databinding.FragmentPage1HomeBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

class HomeFragment : BaseFragment() {

    private var _binding : FragmentPage1HomeBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage1HomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.homeFragmentOnResume()
    }


}