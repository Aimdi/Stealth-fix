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
 *
 * To reduce the odds of being blocked or rate-limited, each token is requested with a user agent
 * picked from a pool of real Reddit for Android versions, and acquisition is retried with a short
 * backoff. The user agent used to obtain a token is reused for the API requests made with it, so
 * the two stay consistent.
 */
@Singleton
class RedditOAuthTokenProvider @Inject constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()

    private var accessToken: String? = null
    private var expirationTime: Long = 0L
    private var userAgentIndex: Int = 0

    /** User agent that obtained the current token; reused for API calls made with it. */
    var userAgent: String = USER_AGENTS.first()
        private set

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
        var delay = INITIAL_RETRY_DELAY

        repeat(MAX_ATTEMPTS) { attempt ->
            // Rotate the user agent for each acquisition to vary the client fingerprint
            val candidateUserAgent = USER_AGENTS[userAgentIndex % USER_AGENTS.size]
            userAgentIndex++

            if (tryRefresh(candidateUserAgent)) {
                return
            }

            if (attempt < MAX_ATTEMPTS - 1) {
                try {
                    Thread.sleep(delay)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return
                }
                delay *= 2
            }
        }
    }

    /** Requests a token with the given user agent. Returns true on success. */
    private fun tryRefresh(candidateUserAgent: String): Boolean {
        val deviceId = UUID.randomUUID().toString()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(TOKEN_SCOPES.toRequestBody(MEDIA_TYPE_JSON))
            .header("Authorization", Credentials.basic(CLIENT_ID, ""))
            .header("User-Agent", candidateUserAgent)
            .header("Client-Vendor-Id", deviceId)
            .header("X-Reddit-Device-Id", deviceId)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Retry on rate limits and server errors; give up on other client errors
                    return response.code == HTTP_TOO_MANY_REQUESTS || response.code >= 500
                }

                val body = response.body?.string() ?: return true

                val json = JSONObject(body)
                val token = json.optString("access_token")
                if (token.isEmpty()) {
                    return true
                }

                accessToken = token
                userAgent = candidateUserAgent
                expirationTime = System.currentTimeMillis() +
                        (json.optLong("expires_in", DEFAULT_EXPIRATION) - EXPIRATION_MARGIN) * 1000

                loid = response.header(HEADER_LOID) ?: loid
                session = response.header(HEADER_SESSION) ?: session

                true
            }
        } catch (e: IOException) {
            false // Transient network error: allow a retry
        } catch (e: JSONException) {
            true // Malformed response: retrying won't help
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

        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY = 1000L // ms
        private const val HTTP_TOO_MANY_REQUESTS = 429

        const val HEADER_LOID = "x-reddit-loid"
        const val HEADER_SESSION = "x-reddit-session"

        // Pool of real Reddit for Android versions used as user agents
        private val USER_AGENTS = listOf(
            "Reddit/Version 2024.22.1/Build 1652272/Android 13",
            "Reddit/Version 2024.23.1/Build 1665606/Android 13",
            "Reddit/Version 2024.24.1/Build 1682520/Android 14",
            "Reddit/Version 2024.25.2/Build 1700401/Android 14",
            "Reddit/Version 2024.26.1/Build 1719363/Android 12"
        )
    }
}
