package com.delgado.reportesapp.utils

object Constants {
    /**
     * URL base centralizada de la API pública desplegada en Render.com.
     */
    const val BASE_URL = "https://reporteservice.onrender.com/"

    /**
     * Tamaño de página por defecto para paginación de reportes.
     */
    const val DEFAULT_LIMIT = 5

    /**
     * Extras para la transferencia de datos entre actividades.
     */
    const val EXTRA_REPORTE_ID = "EXTRA_REPORTE_ID"
    const val EXTRA_REPORTE = "EXTRA_REPORTE"
}
