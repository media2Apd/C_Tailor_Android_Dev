package com.example.cusotailor.repository

import com.example.cusotailor.data.UserDao
import com.example.cusotailor.data.User
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.SignupResponse


class UserRepository(private val cusoDao: UserDao) {

    suspend fun login(email: String, password: String): User? {
        return cusoDao.login(email, password)
    }

    suspend fun getUserByEmail(email: String): User? {
        return cusoDao.getUserByEmail(email)
    }

    suspend fun registerUser(user: User): Boolean {
        val existing = cusoDao.getUserByEmail(user.email)

        return if (existing == null) {
            cusoDao.insertUser(user)
            true
        } else {
            false
        }
    }

    suspend fun createAccount(request: SignupRequest): Result<SignupResponse>{
        return try{
            val response=RetrofitClient.apiService.signup(request)
            if(response.isSuccessful && response.body()!=null ){
                Result.success(response.body()!!)
            }
            else{
                Result.failure(Exception("Error ${response.code()} - ${response.message()} "))
            }
        }catch (e:Exception){
            Result.failure(Exception("Network error: ${e.message} "))
        }

    }
}