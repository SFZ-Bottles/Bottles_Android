package online.bottles.ui.main.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.R
import online.bottles.databinding.FragmentPage3MessageBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import online.bottles.ui.main.items.message.DirectMessage

class MessageFragment : BaseFragment() {

    private var _binding : FragmentPage3MessageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage3MessageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.messageFragmentOnResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchUserProfile.setOnClickListener(){
            val SendMessage= DirectMessage()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.page, SendMessage)
            transaction.addToBackStack(null)
            transaction.commit()

        }
    }
}