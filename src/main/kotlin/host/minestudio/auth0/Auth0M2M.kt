package host.minestudio.auth0

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import host.minestudio.auth0.util.JSONRequest

class Auth0M2M(
    private val clientId: String,
    private val clientSecret: String,
    private val audience: String,
    private val domain: String
) {

    var authToken: String = ""
    var authTokenType: String = ""
    lateinit var jsonReq: JsonObject;
    fun getAuthToken(): JsonObject {
        if(authToken.isEmpty()) {
            try {
                val body = JsonObject()
                body.addProperty("client_id", clientId)
                body.addProperty("client_secret", clientSecret)
                body.addProperty("audience", audience)
                body.addProperty("grant_type", "client_credentials")
                val req = JSONRequest.request(
                    "https://$domain/oauth/token",
                    "POST",
                    hashMapOf(
                        "Content-Type" to "application/json"
                    ),
                    true,
                    body.toString().toByteArray()
                )
                try {
                    authToken = req.get("access_token").asString
                    authTokenType = req.get("token_type").asString
                    jsonReq = req
                    return req;
                } catch (e: Exception) {
                    return req;
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }
        return jsonReq
    }
}