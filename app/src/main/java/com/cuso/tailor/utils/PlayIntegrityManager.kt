package com.cuso.tailor.utils
//
//import android.content.Context
//import com.cuso.tailor.BuildConfig
//import com.google.android.gms.tasks.Task
//import com.google.android.play.core.integrity.IntegrityManagerFactory
//import com.google.android.play.core.integrity.StandardIntegrityManager
//import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
//import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
//import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import java.security.MessageDigest
//import javax.inject.Inject
//import javax.inject.Singleton
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//
//@Singleton
//class PlayIntegrityManager @Inject constructor(
//    @ApplicationContext private val context: Context
//) {
//    private val standardIntegrityManager: StandardIntegrityManager by lazy {
//        IntegrityManagerFactory.createStandard(context)
//    }
//
//    private var tokenProvider: StandardIntegrityTokenProvider? = null
//    private val prepareMutex = Mutex()
//
//    suspend fun prepare(): Boolean = prepareMutex.withLock {
//        if (tokenProvider != null) return true
//        return try {
//            val request = PrepareIntegrityTokenRequest.builder()
//                .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_NUMBER)
//                .build()
//
//            tokenProvider = standardIntegrityManager
//                .prepareIntegrityToken(request)
//                .awaitResult()
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    suspend fun getIntegrityToken(rawRequestData: String): String? {
//        return try {
//            if (tokenProvider == null) {
//                val prepared = prepare()
//                if (!prepared) return null
//            }
//
//            val requestHash = sha256Hex(rawRequestData)
//            val request = StandardIntegrityTokenRequest.builder()
//                .setRequestHash(requestHash)
//                .build()
//
//            val response = tokenProvider!!.request(request).awaitResult()
//            response.token()
//        } catch (e: Exception) {
//            tokenProvider = null
//            null
//        }
//    }
//
//    private fun sha256Hex(input: String): String {
//        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
//        return bytes.joinToString("") { "%02x".format(it) }
//    }
//
//    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
//        addOnSuccessListener { result ->
//            if (cont.isActive) cont.resume(result)
//        }
//        addOnFailureListener { exception ->
//            if (cont.isActive) cont.resumeWithException(exception)
//        }
//    }
//}