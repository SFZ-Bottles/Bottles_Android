package online.bottles.ui.main.items.home

data class AlbumsResponse(
    val num: Int,
    val results: List<AlbumResponse>
)

data class AlbumResponse(
    val id: String,
    val user_id: String,
    val cover_image_url: String,
    val title: String,
    val created_at: String
)