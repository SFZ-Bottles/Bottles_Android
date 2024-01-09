package online.bottles.ui.main.items.search

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface searchUsears {
    @GET("/api/search/user/")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("num") num: Int,
        @Header("Authorization") authToken: String
    ): Response<SearchResponse>
}

data class SearchResponse(
    @SerializedName("message") val message : String,
    @SerializedName("num") val num : Int,
    @SerializedName("result") val result : List<UserResult>
)

data class UserResult(
    @SerializedName("id") val id :String,
    @SerializedName("info") val info : String,
    @SerializedName("avatar") val avatar : String
)