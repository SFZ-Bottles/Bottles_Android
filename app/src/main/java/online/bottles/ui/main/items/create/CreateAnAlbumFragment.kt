package online.bottles.ui.main.items.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import online.bottles.databinding.FragmentCreateAnAlbumBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity

class CreateAnAlbumFragment : BaseFragment() {
    private lateinit var binding: FragmentCreateAnAlbumBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentCreateAnAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.createAnAlbumFragmentOnResume()
    }

    override fun onStop() {
        (activity as? MainActivity)?.myPageFragmentOnResume()
        super.onStop()
    }

}