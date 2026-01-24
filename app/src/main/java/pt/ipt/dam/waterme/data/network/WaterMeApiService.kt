package pt.ipt.dam.waterme.data.network

import pt.ipt.dam.waterme.data.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Interface que define os endpoints da API REST (Backend).
 * Utilizamos a biblioteca Retrofit para transformar estas funções em chamadas HTTP reais.
 *
 * Cada função mapeia um tipo de pedido HTTP (GET, POST, PUT, DELETE) para um URL específico.
 */
interface WaterMeApiService {

    /**
     * Obtém a lista de todas as plantas de um utilizador específico.
     * @param userId O ID do utilizador cujas plantas queremos listar.
     * @return Uma lista de objetos PlantResponse vindos do servidor.
     */
    @GET("plants/{user_id}")
    suspend fun getPlants(@Path("user_id") userId: Int): List<PlantResponse>

    /**
     * Envia uma nova planta para ser guardada no servidor.
     * @param plant O objeto com os dados da planta.
     * @return A resposta do servidor, que inclui o ID gerado para a nova planta.
     */
    @POST("plants")
    suspend fun addPlant(@Body plant: PlantRequest): PlantResponse

    /**
     * Atualiza os dados de uma planta existente.
     * @param id O ID da planta a atualizar (vai no URL).
     * @param plant Os novos dados da planta (vai no corpo).
     * @return O objeto atualizado confirmado pelo servidor.
     */
    @PUT("plants/{id}")
    suspend fun updatePlant(@Path("id") id: Int, @Body plant: PlantRequest): PlantResponse

    /**
     * Apaga uma planta do servidor permanentemente.
     * @param id O ID da planta a eliminar.
     */
    @DELETE("plants/{id}")
    suspend fun deletePlant(@Path("id") id: Int)


    // --- AUTENTICAÇÃO ---

    /**
     * Realiza o login do utilizador.
     * Envia email e password e recebe o ID e nome do utilizador se as credenciais estiverem corretas.
     * @param request Objeto com email e password.
     * @return Objeto LoginResponse com os dados do utilizador.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * Regista um novo utilizador na aplicação.
     * @param request Objeto com nome, email e password.
     * @return Objeto RegisterResponse com o ID do novo utilizador.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    /**
     * Altera a palavra-passe do utilizador.
     * Retorna um objeto Response genérico para podermos verificar o código de estado HTTP (ex: 200 OK ou 401 Unauthorized).
     * @param request Objeto com o ID do user, a senha antiga e a nova senha.
     */
    @PUT("/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, String>>
}

/**
 * Objeto Singleton (único na app) para gerir a instância do Retrofit.
 * Serve para configurar a ligação ao servidor uma única vez e reutilizá-la em toda a aplicação.
 */
object RetrofitClient {
    // Endereço base da API (Backend alojado no Render)
    private const val BASE_URL = "https://waterme-2l72.onrender.com/"

    /**
     * Cria e configura a instância da API.
     * A utilização de 'by lazy' significa que o objeto só é criado na primeira vez que for chamado,
     * poupando recursos de memória se a app abrir mas não usar a rede imediatamente.
     */
    val api: WaterMeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Adiciona o conversor GSON para transformar automaticamente JSON em objetos Kotlin e vice-versa
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WaterMeApiService::class.java)
    }
}