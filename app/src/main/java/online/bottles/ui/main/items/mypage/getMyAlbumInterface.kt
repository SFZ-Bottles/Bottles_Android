package online.bottles.ui.main.items.mypage

import online.bottles.ui.main.items.home.AlbumsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface getMyAlbumInterface {
    @GET("/api/albums/")
    suspend fun getAlbums(
        @Query("is_private") isPrivate: Boolean,
        @Query("num") num: Int,
        @Query("count") count: Int,
        @Query("target") target: String,
        @Query("order_by") orderBy: String,
        @Header("Authorization") authToken: String
    ): AlbumsResponse
}