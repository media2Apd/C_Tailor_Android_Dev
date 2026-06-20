
import android.util.Log
import com.cuso.mobile.network.ApiService
//import com.google.firebase.appdistribution.gradle.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


private val loggingInterceptor = HttpLoggingInterceptor { message ->
    Log.d("OkHttp", message)
}.apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val client = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

// then pass `client` into your Retrofit.Builder().client(client)...
object RetrofitClient {

    private const val BASE_URL = "http://10.33.173.65:5000"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}