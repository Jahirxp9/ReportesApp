package com.delgado.reportesapp.data.repository

import com.delgado.reportesapp.data.model.Reporte
import com.delgado.reportesapp.data.model.ReporteRequest
import com.delgado.reportesapp.data.remote.RetrofitClient
import com.delgado.reportesapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para la gestión de datos de reportes conectados a la API REST Spring Boot.
 */
class ReporteRepository {

    private val api = RetrofitClient.apiService

    /**
     * Obtener lista de reportes paginados (GET /reportes/?offset={offset}&limit={limit})
     */
    suspend fun getReportesPaginados(offset: Int, limit: Int = 5): Resource<List<Reporte>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getReportesPaginados(offset, limit)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cargar reportes (Código ${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("No se pudo conectar con el servidor: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    /**
     * Registrar un nuevo reporte (POST /reportes/) enviando solo los datos de usuario
     */
    suspend fun registrarReporte(request: ReporteRequest): Resource<Reporte> = withContext(Dispatchers.IO) {
        try {
            val response = api.registrarReporte(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al registrar reporte (Código ${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("No se pudo conectar con el servidor: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    /**
     * Consultar un reporte por su ID (GET /reportes/{id})
     */
    suspend fun getReporteById(id: Int): Resource<Reporte> = withContext(Dispatchers.IO) {
        try {
            val response = api.getReporteById(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("No se encontró el reporte con ID $id")
            }
        } catch (e: Exception) {
            Resource.Error("No se pudo conectar con el servidor: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    /**
     * Consultar reportes por DNI de ciudadano (GET /reportes/ciudadano/{dni})
     */
    suspend fun getReportesByDni(dni: String): Resource<List<Reporte>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getReportesByDni(dni)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("No se encontraron reportes para el DNI $dni")
            }
        } catch (e: Exception) {
            Resource.Error("No se pudo conectar con el servidor: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    /**
     * Marcar reporte como atendido (PUT /reportes/{id})
     */
    suspend fun atenderReporte(id: Int): Resource<Reporte> = withContext(Dispatchers.IO) {
        try {
            val response = api.atenderReporte(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al marcar el reporte como atendido (Código ${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("No se pudo conectar con el servidor: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }
}
