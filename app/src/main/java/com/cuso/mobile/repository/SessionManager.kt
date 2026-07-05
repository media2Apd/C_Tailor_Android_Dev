package com.cuso.mobile.repository

import com.cuso.mobile.database.dao.TokensDao
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

    // Called when user taps Logout
    suspend fun logout() {
        tokensDao.clearTokens()
    }
}