package host.minestudio.auth0

import host.minestudio.auth0.util.JSONRequest

class Auth0M2M(
    val clientId: String,
    val clientSecret: String,
    val audience: String,
    val domain: String
) {

    private var authToken: String = ""
    private var authTokenType: String = ""
    fun getAuthToken(): String {
        if(authToken.isEmpty()) {
            val req = JSONRequest.request(
                "https://$domain/oauth/token",
                "POST",
                hashMapOf(
                    "Content-Type" to "application/json"
                ),
                true,
                """
                    {
                        "client_id": "$clientId",
                        "client_secret": "$clientSecret",
                        "audience": "$audience",
                        "grant_type": "client_credentials"
                    }
                """.trimIndent().toByteArray()
            )
            authToken = req.get("access_token").asString
            authTokenType = req.get("token_type").asString
        }
        return authToken
    }
}