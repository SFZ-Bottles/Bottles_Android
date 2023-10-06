package online.bottles.api.response

import com.google.gson.annotations.SerializedName

data class loginJWT (
    @SerializedName("token")
    val token: String
)