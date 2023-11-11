package online.bottles.api.response

data class albumResponse(val error:String?)
data class albumText(val text:String?)
data class albumImage(val image:String?)
data class albumVideo(val video:String?)
data class albumTitle(
    val title:String?,
    val preface:String?,
    val titleImage:String?)