package com.delgado.reportesapp.ui.detalle

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.delgado.reportesapp.R
import com.delgado.reportesapp.data.model.Reporte
import com.delgado.reportesapp.data.repository.ReporteRepository
import com.delgado.reportesapp.databinding.ActivityDetalleReporteBinding
import com.delgado.reportesapp.utils.Constants
import com.delgado.reportesapp.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DetalleReporteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleReporteBinding
    private val repository = ReporteRepository()
    private var reporteActual: Reporte? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        obtenerReporteDeIntent()
        setupListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun obtenerReporteDeIntent() {
        @Suppress("DEPRECATION")
        reporteActual = intent.getSerializableExtra(Constants.EXTRA_REPORTE) as? Reporte

        val reporteId = intent.getIntExtra(Constants.EXTRA_REPORTE_ID, -1)

        if (reporteActual != null) {
            mostrarDetalles(reporteActual!!)
        } else if (reporteId != -1) {
            cargarReportePorId(reporteId)
        } else {
            Toast.makeText(this, "No se especificó un reporte válido", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarReportePorId(id: Int) {
        mostrarCargando(true)
        lifecycleScope.launch {
            when (val res = repository.getReporteById(id)) {
                is Resource.Success -> {
                    mostrarCargando(false)
                    res.data?.let {
                        reporteActual = it
                        mostrarDetalles(it)
                    }
                }
                is Resource.Error -> {
                    mostrarCargando(false)
                    Toast.makeText(this@DetalleReporteActivity, res.message ?: "Error al cargar reporte", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> mostrarCargando(true)
            }
        }
    }

    private fun mostrarDetalles(reporte: Reporte) {
        binding.tvDetalleId.text = if (reporte.id != null) {
            getString(R.string.label_codigo_format, reporte.id)
        } else {
            "Reporte sin ID"
        }

        binding.tvDetalleDni.text = reporte.dni
        binding.tvDetalleFecha.text = reporte.fechaFormateada
        binding.tvDetalleTipo.text = reporte.tipo
        binding.tvDetalleDireccion.text = reporte.direccion
        binding.tvDetalleReferencia.text = reporte.referencia ?: "-"

        actualizarEstadoUi(reporte.estado)
    }

    private fun actualizarEstadoUi(estado: Int) {
        if (estado == 1) {
            binding.tvDetalleEstado.text = getString(R.string.estado_pendiente)
            binding.tvDetalleEstado.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_pendiente)
            binding.tvDetalleEstado.setTextColor(ContextCompat.getColor(this, R.color.status_pendiente_text))
            binding.btnMarcarAtendido.visibility = View.VISIBLE
            binding.btnMarcarAtendido.isEnabled = true
        } else {
            binding.tvDetalleEstado.text = getString(R.string.estado_atendido)
            binding.tvDetalleEstado.background = ContextCompat.getDrawable(this, R.drawable.bg_badge_atendido)
            binding.tvDetalleEstado.setTextColor(ContextCompat.getColor(this, R.color.status_atendido_text))
            binding.btnMarcarAtendido.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnMarcarAtendido.setOnClickListener {
            confirmarAtencionReporte()
        }
    }

    private fun confirmarAtencionReporte() {
        val reporteId = reporteActual?.id ?: run {
            Toast.makeText(this, "El ID del reporte no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_confirmar_atención_titulo)
            .setMessage(R.string.dialog_confirmar_atención_mensaje)
            .setPositiveButton(R.string.btn_confirmar) { _, _ ->
                ejecutarAtenderReporte(reporteId)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun ejecutarAtenderReporte(id: Int) {
        mostrarCargando(true)

        lifecycleScope.launch {
            val res = repository.atenderReporte(id)
            mostrarCargando(false)

            when (res) {
                is Resource.Success -> {
                    Toast.makeText(this@DetalleReporteActivity, R.string.msg_reporte_atendido, Toast.LENGTH_SHORT).show()
                    val reporteActualizado = res.data ?: reporteActual?.copy(estado = 0)
                    reporteActualizado?.let {
                        reporteActual = it
                        actualizarEstadoUi(it.estado)
                    }
                    setResult(Activity.RESULT_OK)
                }
                is Resource.Error -> {
                    Toast.makeText(this@DetalleReporteActivity, res.message ?: "Error al atender reporte", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> mostrarCargando(true)
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnMarcarAtendido.isEnabled = !cargando
    }
}
