package online.bottles.ui.main.items


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.databinding.FragmentPage2SearchBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

class SearchFragment : BaseFragment(){

    private var _binding : FragmentPage2SearchBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage2SearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.searchFragmentOnResume()
    }



}