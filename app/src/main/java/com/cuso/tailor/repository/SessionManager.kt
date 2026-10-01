package com.cuso.tailor.repository

import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.utils.AuthEventManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokensDao: TokensDao
) {
    // Called on app start - checks Room DB for existing valid token
    suspend fun isLoggedIn(): Boolean {
        val tokens = tokensDao.getTokens()
        return !tokens?.accessToken.isNullOrEmpty()
    }

    // Helper to get current access token
    suspend fun getAccessToken(): String? {
        return tokensDao.getTokens()?.accessToken
    }

    // Called when user explicitly taps Logout
    suspend fun logout() {
        tokensDao.clearTokens()
    }

    // Called automatically when an API returns 401 / session expired
    suspend fun onSessionExpired(message: String = "Session expired. Please log in again.") {
        // 1. Wipe cached tokens from Room DB
        tokensDao.clearTokens()

        // 2. Broadcast the event globally to redirect to the Login screen
        AuthEventManager.onSessionExpired(message)
    }
}