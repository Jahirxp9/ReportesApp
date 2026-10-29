package com.delgado.reportesapp.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Modelo de datos que representa un reporte de acumulación de basura en la app Android.
 */
data class Reporte(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("dni")
    val dni: String,

    @SerializedName("fecha")
    val fecha: String? = null,

    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("referencia")
    val referencia: String? = null,

    @SerializedName("estado")
    val estado: Int = 1
) : Serializable {

    val esPendiente: Boolean
        get() = estado == 1

    val estadoTexto: String
        get() = if (estado == 1) "PENDIENTE" else "ATENDIDO"

    val fechaFormateada: String
        get() {
            if (fecha.isNullOrBlank()) return "-"
            return fecha.replace("T", " ")
        }
}
