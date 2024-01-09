package online.bottles.ui.main.items.home
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface getAlbums {
    @GET("/api/albums/")
    suspend fun getAlbums(
        @Query("is_private") isPrivate : Boolean,
        @Query("num") num: Int,
        @Query("counts") counts: Int,
        @Query("target") target: String,
        @Query("order_by") orderBy :String,
        @Header("Authorization") authToken: String
    ): AlbumsResponse
}