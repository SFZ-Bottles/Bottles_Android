package online.bottles.ui.main.items.search


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import online.bottles.R
import online.bottles.api.response.albumResponse
import online.bottles.api.response.bottlesUrl
import online.bottles.api.response.loginResponse
import online.bottles.databinding.FragmentPage2SearchBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import online.bottles.ui.main.items.home.getAlbums
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : BaseFragment(){

    private val bottlesURL = bottlesUrl.bottlesSearch
    private var _binding : FragmentPage2SearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: searchUsears
    private lateinit var authToken: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage2SearchBinding.inflate(inflater, container, false)
        authToken = getAuthToken()
        Log.d("AuthToken", "Token: $authToken")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRetrofit()
        binding.searchEdit.addTextChangedListener(object : TextWatcher{  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // 텍스트 변경 전에 수행할 작업
        }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 수행할 작업
                val searchText = s.toString()
                performSearch(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString()
                if (searchText.isBlank()) {
                    binding.searchUserProfile.removeAllViews()
                }
            }})
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.searchFragmentOnResume()
    }
    private fun performSearch(query: String){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = SearchApi(apiService).searchUsers(query, 10, authToken)

                if (response.isSuccessful) {
                    val searchResults = response.body()?.result ?: emptyList()
                    // 검색 결과를 사용해 UI 갱신 등의 작업 수행

                    updateSearchResults(searchResults)
                } else {
                    // 서버 응답이 실패한 경우, 에러 메시지를 로그에 출력하거나 사용자에게 피드백을 줄 수 있습니다.
                    Log.e("SearchError", "Server error: ${response.message()}")
                    // 여기에 사용자에게 알림을 주거나 다른 예외 처리 로직을 추가
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 오류 또는 예외 처리
                Log.e("NetworkError", "Search error: ${e.message}")
                // 여기에 사용자에게 알림을 주거나 다른 예외 처리 로직을 추가
                return@launch
            }
        }
    }
    private fun updateSearchResults(results: List<UserResult>) {
        // 여기에서 검색 결과를 사용해 UI를 업데이트
        Log.d("ShowUsers","Result is not empty")
        binding.searchUserProfile.removeAllViews()
        if(results.isNotEmpty()){
            for (searchResponse in results) {
                // 새로운 뷰를 추가
                val includeTemplate =
                    LayoutInflater.from(requireContext()).inflate(R.layout.bottles_search_user, null)
                val setProfUserImage =
                    includeTemplate.findViewById<ImageView>(R.id.profUserImage)
                val setUserProfileName =
                    includeTemplate.findViewById<TextView>(R.id.userProfileName)
                val setUserProfileIntro =
                    includeTemplate.findViewById<TextView>(R.id.userProfileIntro)

                binding.searchUserProfile.addView(includeTemplate)


                loadImageWithGlide(searchResponse.avatar, setProfUserImage)

                setUserProfileName.text = searchResponse.id
                setUserProfileIntro.text = searchResponse.info
            }
        }
    }
    private fun loadImageWithGlide(imageUrl: String, imageView: ImageView) {
        Glide.with(requireActivity())
            .load(imageUrl)
            .into(imageView)
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

        apiService = retrofit.create(searchUsears::class.java)
    }
    class SearchApi(private val apiService: searchUsears){
        suspend fun searchUsers(
            q : String,
            num : Int,
            authToken : String
        ): Response<SearchResponse>{
            return apiService.searchUsers(q,num,authToken)
        }
    }



}