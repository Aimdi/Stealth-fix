package com.cosmos.unreddit.data.remote.api.reddit.auth

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides anonymous OAuth access tokens for the official Reddit API.
 *
 * Reddit no longer serves its public .json endpoints to third-party clients. To keep the app
 * account-free, tokens are requested the same way the official Android app does for logged-out
 * users, and requests are then sent to oauth.reddit.com.
 */
@Singleton
class RedditOAuthTokenProvider @Inject constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()

    private var accessToken: String? = null
    private var expirationTime: Long = 0L

    var loid: String? = null
        private set

    var session: String? = null
        private set

    @Synchronized
    fun getAccessToken(): String? {
        if (accessToken == null || System.currentTimeMillis() >= expirationTime) {
            refreshToken()
        }
        return accessToken
    }

    @Synchronized
    fun invalidateToken(token: String) {
        if (token == accessToken) {
            accessToken = null
        }
    }

    private fun refreshToken() {
        val deviceId = UUID.randomUUID().toString()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(TOKEN_SCOPES.toRequestBody(MEDIA_TYPE_JSON))
            .header("Authorization", Credentials.basic(CLIENT_ID, ""))
            .header("User-Agent", USER_AGENT)
            .header("Client-Vendor-Id", deviceId)
            .header("X-Reddit-Device-Id", deviceId)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return
                }

                val body = response.body?.string() ?: return

                val json = JSONObject(body)
                val token = json.optString("access_token")
                if (token.isEmpty()) {
                    return
                }

                accessToken = token
                expirationTime = System.currentTimeMillis() +
                        (json.optLong("expires_in", DEFAULT_EXPIRATION) - EXPIRATION_MARGIN) * 1000

                loid = response.header(HEADER_LOID) ?: loid
                session = response.header(HEADER_SESSION) ?: session
            }
        } catch (e: IOException) {
            // Keep the previous state; a new token will be requested with the next call
        } catch (e: JSONException) {
            // Ignore malformed responses
        }
    }

    companion object {
        private const val TOKEN_URL = "https://www.reddit.com/auth/v2/oauth/access-token/loid"

        // Client id of the official Reddit app for Android
        private const val CLIENT_ID = "ohXpoqrZYub1kg"

        private const val TOKEN_SCOPES = "{\"scopes\": [\"*\", \"email\", \"pii\"]}"

        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

        private const val TIMEOUT = 30L

        private const val DEFAULT_EXPIRATION = 86400L // seconds
        private const val EXPIRATION_MARGIN = 60L // seconds

        const val HEADER_LOID = "x-reddit-loid"
        const val HEADER_SESSION = "x-reddit-session"

        const val USER_AGENT = "Reddit/Version 2024.22.1/Build 1652272/Android 13"
    }
}
