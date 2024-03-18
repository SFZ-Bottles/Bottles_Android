package online.bottles.ui.main.items.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.internal.ViewUtils.dpToPx
import com.google.android.material.shape.ShapeAppearanceModel.CornerSizeUnaryOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.bottles.R
import online.bottles.api.response.bottlesUrl
import online.bottles.api.response.bottlesUrl.bottlesURL
import online.bottles.databinding.FragmentPage4MyPageBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import online.bottles.ui.main.items.follow.FollowerFragment
import online.bottles.ui.main.items.follow.FollowingFragment
import online.bottles.ui.main.items.home.AlbumsResponse
import online.bottles.ui.main.items.home.getAlbums
import online.bottles.ui.main.items.option.GetUserProfile
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class MyPageFragment : BaseFragment() {
    private lateinit var userId : String
    private var _binding : FragmentPage4MyPageBinding?= null
    private lateinit var authToken: String
    private lateinit var apiService : getMyAlbumInterface
    private lateinit var getUserInterfaceApi : GetUserProfile
    private var counts = 1
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPage4MyPageBinding.inflate(inflater, container, false)
        userId = getSharedUserInterface().orEmpty()
        authToken = getAuthToken()
        return binding.root
    }

    private fun getSharedUserInterface(): String? {
        val sharedPreferencesForId = activity?.getSharedPreferences("user", Context.MODE_PRIVATE)
        return sharedPreferencesForId?.getString("userId", null).orEmpty()
    }//내 id 가져옴
    private fun getAuthToken(): String {
        // Fragment에 연결된 Activity를 통해 SharedPreferences를 가져오도록
        val sharedPreferences = activity?.getSharedPreferences("jwt_token", Context.MODE_PRIVATE)
        val authToken = sharedPreferences?.getString("jwt_token",null).orEmpty()
        return authToken
    }
    override fun onResume() {
        (activity as? MainActivity)?.myPageFragmentOnResume()
        super.onResume()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRetrofit()
        getMyProfile()
        getMyAlbums(authToken)
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
    private fun getMyAlbums(authToken:String){
        CoroutineScope(Dispatchers.Main).launch {
            try{
                val response = CoroutineScope(Dispatchers.Default).async {
                    MyRepository(apiService).getAlbums(
                        false,
                        8,
                        counts,
                        userId,
                        "-created_at",
                        authToken)
                }.await()
                Log.d ("getMyAlbums","Response: $response")
                showAlbums(response)
                counts += 8
            }catch (e:Exception){
                e.printStackTrace()
                Log.e("NetworkError","Network error: ${e.message}")
            }
        }
    }
    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private fun showAlbums(albumData : AlbumsResponse){
        Log.d("ShowAlbums","ShowAlbums function called on thread: ${Thread.currentThread().name}")
        var rfmyPage = binding.homeRefreshLayout
        if(albumData.results.isNotEmpty()){
            rfmyPage.visibility = View.GONE
            Log.d("showAlbums","result is not empty")
            albumData.results.forEach{ album ->
                val imageView = ImageView(context).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0 // GridLayout에서는 이 값을 0으로 설정
                        height = dpToPx(180) // 높이를 180dp로 변환
                        val setMargin = dpToPx(5)
                        setMargins(setMargin,setMargin,setMargin,setMargin)
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    }
                    scaleType = ImageView.ScaleType.FIT_XY
                }
                Glide.with(this@MyPageFragment)
                    .load(album.cover_image_url)
                    .into(imageView)

                // GridLayout에 ImageView 추가
                binding.imageGrid.addView(imageView)
            }


        }
    }
    private fun getMyProfile(){
        val userid = userId
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = getUserInterfaceApi.userProfile(userid,authToken)
                if(response.isSuccessful){
                    val userProfile = response.body()
                    withContext(Dispatchers.Main){
                        userProfile?.avatar?.let { avatarUrl ->
                            Glide.with(this@MyPageFragment)
                                .load(avatarUrl)
                                .into(binding.profUserImage)
                        }
                        binding.profUserName.text = userProfile?.name
                        binding.profUserPref.text = userProfile?.info
                    }
                }
                else{
                    Log.e("userProfileError","Error:${response.errorBody()?.string()}")
                }
            }catch (e:Exception){
                e.printStackTrace()
                Log.e("NetworkError","Error: ${e.message}")
                binding.homeRefreshButton.visibility = VISIBLE
            }
        }
    }
    private fun initRetrofit(){
        val retrofit = Retrofit.Builder()
            .baseUrl(bottlesURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(getMyAlbumInterface::class.java)
        getUserInterfaceApi = retrofit.create(GetUserProfile::class.java)

    }
    class MyRepository(private val apiService: getMyAlbumInterface){
        suspend fun getAlbums(
            isPrivate : Boolean,
            num : Int,
            counts : Int,
            target : String,
            orderBy : String,
            authToken: String
        ): AlbumsResponse{
            return apiService.getAlbums(isPrivate,num,counts,target,orderBy,authToken)
        }
    }


}
