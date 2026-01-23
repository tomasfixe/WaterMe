package pt.ipt.dam.waterme.data.network

import pt.ipt.dam.waterme.data.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface WaterMeApiService {


    @GET("plants/{user_id}")
    suspend fun getPlants(@Path("user_id") userId: Int): List<PlantResponse>

    @POST("plants")
    suspend fun addPlant(@Body plant: PlantRequest): PlantResponse

    @PUT("plants/{id}")
    suspend fun updatePlant(@Path("id") id: Int, @Body plant: PlantRequest): PlantResponse

    @DELETE("plants/{id}")
    suspend fun deletePlant(@Path("id") id: Int)


    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @PUT("/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, String>>
}

object RetrofitClient {
    private const val BASE_URL = "https://waterme-2l72.onrender.com/"

    val api: WaterMeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WaterMeApiService::class.java)
    }
}