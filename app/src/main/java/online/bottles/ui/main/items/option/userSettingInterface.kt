package online.bottles.ui.main.items.option


import android.net.Uri
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GetUserProfile{
    @GET("/api/users/{id}")
    suspend fun userProfile(
        @Path("id") userid : String,
        @Header("Authorization") authToken: String
    ): Response<GetUserInterface>

    @PUT("api/users/{id}")
    suspend fun setUserProfile(
        @Path("id") userId: String,
        @Header("Authorization") authToken: CharSequence,
        @Query("id") id: String,
        @Query("password") password : String,
        @Query("name") name: CharSequence,
        @Query("email") email: CharSequence,
        @Query("info") info: CharSequence,
        @Query("avatar") avatar: Uri?
    ):Response<SetUserInterface>

    @PUT("api/users/{id}")
    suspend fun setUserProfileWithoutPassword(
        @Path("id") userId: String,
        @Header("Authorization") authToken: CharSequence,
        @Query("id") id: String,
        @Query("name") name: CharSequence,
        @Query("email") email: CharSequence,
        @Query("info") info: CharSequence,
        @Query("avatar") avatar: Uri?
    ): Response<SetUserInterface>
}
data class GetUserInterface(
    @SerializedName("id") val id : String,
    @SerializedName("name") val name : String,
    @SerializedName("email") val email : String,
    @SerializedName("info") val info : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("avatar") val avatar : String
)
data class SetUserInterface(
    @SerializedName("token") val newToken : String,
    @SerializedName("id") val id : String,
    @SerializedName("name") val name : String,
    @SerializedName("email") val email : String,
    @SerializedName("info") val info : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("avatar") val avatar : String
)
