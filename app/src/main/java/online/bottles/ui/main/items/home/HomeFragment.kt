
package online.bottles.ui.main.items.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import online.bottles.R
import online.bottles.api.response.bottles
import online.bottles.api.response.bottlesUrl
import online.bottles.databinding.FragmentPage1HomeBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : BaseFragment() {
    private var _binding: FragmentPage1HomeBinding? = null
    private val binding get() = _binding!!

    private val bottlesURL = bottlesUrl.bottlesURL
    private lateinit var apiService: getAlbums
    private lateinit var authToken: String
    private var counts = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage1HomeBinding.inflate(inflater, container, false)
        authToken = getAuthToken()
        Log.d("AuthToken", "Token: $authToken")
        initRetrofit()
        getAlbums(authToken)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeRefreshButton.setOnClickListener(){
            binding.albumScroll.removeAllViews()
            getAlbums(authToken)
        }

    }

    private fun getAuthToken(): String {
        // Fragment에 연결된 Activity를 통해 SharedPreferences를 가져오도록
        val sharedPreferences = activity?.getSharedPreferences("jwt_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences?.getString("jwt_token","token is null").orEmpty()
        Log.d("AuthToken", "Token: $authToken")
        return authToken
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(bottlesURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(getAlbums::class.java)
    }

    private fun showAlbums(albumData: AlbumsResponse) {
        Log.d("ShowAlbums", "showAlbums function called on thread: ${Thread.currentThread().name}")
        var homeRefreLayout = binding.homeRefreshLayout

        if (albumData.results.isNotEmpty()) {
            homeRefreLayout.visibility = View.GONE
            Log.d("ShowAlbums", "Results is not empty")
            var includeTemplate = LayoutInflater.from(requireContext()).inflate(R.layout.bottles_album, null)
            var setImage = includeTemplate.findViewById<ImageView>(R.id.albumImage)
            var setTitle = includeTemplate.findViewById<TextView>(R.id.albumText)
            var setName = includeTemplate.findViewById<TextView>(R.id.albumUserName)
            for (albumResponse in albumData.results) {
                Log.d("ShowAlbums", "Inside loop")
                val includeTemplate = LayoutInflater.from(requireContext()).inflate(R.layout.bottles_album, null) // 루프 내부로 이동
                val setImage = includeTemplate.findViewById<ImageView>(R.id.albumImage)
                val setTitle = includeTemplate.findViewById<TextView>(R.id.albumText)
                val setName = includeTemplate.findViewById<TextView>(R.id.albumUserName)
                loadImageWithGlide(albumResponse.cover_image_url, setImage)
                setTitle.text = albumResponse.title
                setName.text = albumResponse.user_id
                binding.albumScroll.addView(includeTemplate)
            }
        }
    }

    private fun loadImageWithGlide(imageUrl: String, imageView: ImageView) {
        Glide.with(requireActivity())
            .load(imageUrl)
            .into(imageView)
    }

    private fun getAlbums(authToken: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = CoroutineScope(Dispatchers.Default).async {
                    MyRepository(apiService).getAlbums(
                        false,
                        4,
                        counts,
                        "follow",
                        "-created_at",
                        authToken)
                }.await()

                Log.d("GetAlbums", "Response: $response")
                showAlbums(response)
                counts +=4
            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 오류 또는 예외 처리
                Log.e("NetworkError", "Network error: ${e.message}")
                binding.homeRefreshLayout.visibility=View.VISIBLE
            }
        }
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.homeFragmentOnResume()
    }
    class MyRepository(private val apiService: getAlbums) {
        suspend fun getAlbums(
            isPrivate: Boolean,
            num: Int,
            counts: Int,
            target: String,
            orderBy:String,
            authToken: String
        ): AlbumsResponse {
            return apiService.getAlbums(isPrivate,num,counts,target,orderBy,authToken)
        }
    }

}