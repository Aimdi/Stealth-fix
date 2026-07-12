package com.cosmos.unreddit.data.remote.api.reddit

import com.cosmos.unreddit.data.remote.api.reddit.auth.RedditOAuthTokenProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection

/**
 * Authenticates requests to the official Reddit API with an anonymous OAuth token.
 *
 * Authenticated requests are sent to oauth.reddit.com, which serves the same JSON as the
 * public .json endpoints. When no token can be obtained, the request falls back to the public
 * .json endpoint as a best effort.
 */
class OAuthInterceptor(
    private val tokenProvider: RedditOAuthTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val token = tokenProvider.getAccessToken()
            ?: return chain.proceed(request.toPublicJsonRequest())

        val response = chain.proceed(request.toOAuthRequest(token))

        return if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // The token has expired or was revoked; get a fresh one and retry once
            response.close()
            tokenProvider.invalidateToken(token)

            val newToken = tokenProvider.getAccessToken()
                ?: return chain.proceed(request.toPublicJsonRequest())

            chain.proceed(request.toOAuthRequest(newToken))
        } else {
            response
        }
    }

    private fun Request.toOAuthRequest(token: String): Request {
        val oauthUrl = url.newBuilder()
            .host(OAUTH_HOST)
            .build()

        return newBuilder()
            .url(oauthUrl)
            .header("Authorization", "Bearer $token")
            .header("User-Agent", tokenProvider.userAgent)
            .apply {
                tokenProvider.loid?.let { header(RedditOAuthTokenProvider.HEADER_LOID, it) }
                tokenProvider.session?.let { header(RedditOAuthTokenProvider.HEADER_SESSION, it) }
            }
            .build()
    }

    private fun Request.toPublicJsonRequest(): Request {
        val jsonUrl = url.newBuilder()
            .addPathSegment(".json")
            .build()

        return newBuilder()
            .url(jsonUrl)
            .build()
    }

    companion object {
        private const val OAUTH_HOST = "oauth.reddit.com"
    }
}
