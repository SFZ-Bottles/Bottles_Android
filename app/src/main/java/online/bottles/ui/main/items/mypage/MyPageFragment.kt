package online.bottles.ui.main.items.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.bottles.R
import online.bottles.api.response.bottlesUrl
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

class MyPageFragment : BaseFragment() {
    private lateinit var userId : String
    private var _binding : FragmentPage4MyPageBinding?= null
    private lateinit var authToken: String
    private lateinit var getUserInterfaceApi : GetUserProfile
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
                    MyRepository(getUserInterfaceApi).getAlbums()
                }
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
            }
        }
    }
    private fun initRetrofit(){
        val retrofit = Retrofit.Builder()
            .baseUrl(bottlesUrl.bottlesURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        getUserInterfaceApi = retrofit.create(GetUserProfile::class.java)

    }

    class MyRepository(private val apiService:getMyAlbumInterface) {
        suspend fun getAlbums(
            apiService: getMyAlbumInterface,
            isPrivate: Boolean,
            num: Int,
            counts: Int,
            target: String,
            orderBy: String,
            authToken: String
        ): AlbumsResponse {
            return apiService.getAlbums(isPrivate, num, counts, target, orderBy, authToken)
        }
    }
}
