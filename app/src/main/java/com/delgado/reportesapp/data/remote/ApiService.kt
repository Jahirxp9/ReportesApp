package com.delgado.reportesapp.data.remote

import com.delgado.reportesapp.data.model.Reporte
import com.delgado.reportesapp.data.model.ReporteRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit alineada con los 5 endpoints del servidor Spring Boot.
 */
interface ApiService {

    /**
     * 1. POST /reportes/
     * Registra un nuevo reporte sin enviar id, fecha ni estado (el servidor los asigna).
     */
    @POST("reportes/")
    suspend fun registrarReporte(
        @Body reporteRequest: ReporteRequest
    ): Response<Reporte>

    /**
     * 2. GET /reportes/?offset=0&limit=5
     * Obtiene reportes paginados por offset (registros a omitir) y limit.
     */
    @GET("reportes/")
    suspend fun getReportesPaginados(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 5
    ): Response<List<Reporte>>

    /**
     * 3. GET /reportes/{id}
     * Consulta el detalle de un reporte por ID (devuelve 404 si no existe).
     */
    @GET("reportes/{id}")
    suspend fun getReporteById(
        @Path("id") id: Int
    ): Response<Reporte>

    /**
     * 4. GET /reportes/ciudadano/{dni}
     * Consulta los reportes de un ciudadano por DNI (validado a 8 dígitos).
     */
    @GET("reportes/ciudadano/{dni}")
    suspend fun getReportesByDni(
        @Path("dni") dni: String
    ): Response<List<Reporte>>

    /**
     * 5. PUT /reportes/{id}
     * Marca el reporte como atendido (cambia su estado de 1 a 0).
     */
    @PUT("reportes/{id}")
    suspend fun atenderReporte(
        @Path("id") id: Int
    ): Response<Reporte>
}
