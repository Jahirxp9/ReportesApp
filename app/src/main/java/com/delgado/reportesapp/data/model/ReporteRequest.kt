package com.delgado.reportesapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO para el registro de reporte hacia el backend Spring Boot.
 * Cumple con el contrato: no envía id, fecha ni estado (son asignados por el servidor).
 */
data class ReporteRequest(
    @SerializedName("dni")
    val dni: String,

    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("referencia")
    val referencia: String? = null
)
