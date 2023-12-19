
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
import online.bottles.databinding.FragmentPage1HomeBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : BaseFragment() {
    private var _binding: FragmentPage1HomeBinding? = null
    private val binding get() = _binding!!

    private val bottlesURL = "http://14.4.145.80:8000"
    private lateinit var apiService: getAlbums




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage1HomeBinding.inflate(inflater, container, false)
        val authToken = getAuthToken()
        Log.d("AuthToken", "Token: $authToken")
        initRetrofit()
        getAlbums(authToken)

        return binding.root
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

    private fun showAlbums(albumData: List<AlbumResponse>) {
        if (albumData.isNotEmpty()) {
            for (count in 0 until albumData.size) {
                val includeTemplate = layoutInflater.inflate(R.layout.bottles_album, null)
                val setImage = includeTemplate.findViewById<ImageView>(R.id.albumImage)
                val setTitle = includeTemplate.findViewById<TextView>(R.id.albumText)
                val setName = includeTemplate.findViewById<TextView>(R.id.albumUserName)
                binding.albumScroll.addView(includeTemplate)

                loadImageWithGlide(albumData[count].cover_image_url, setImage)
                setTitle.text = albumData[count].title
                setName.text = albumData[count].user_id
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
                    MyRepository(apiService).getAlbums(false, 2, 1, "follow", authToken)
                }.await()

                showAlbums(response.result)
            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 오류 또는 예외 처리
                Log.e("NetworkError", "Network error: ${e.message}")
            }
        }
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.homeFragmentOnResume()
    }
    class MyRepository(private val apiService: getAlbums) {
        suspend fun getAlbums(is_private:Boolean,num:Int,counts:Int,target:String,Authorization: String): AlbumsResponse {
            return apiService.getAlbums(is_private,num,counts,target,Authorization)
        }
    }

}