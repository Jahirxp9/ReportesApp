package com.delgado.reportesapp.ui.registro

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.delgado.reportesapp.R
import com.delgado.reportesapp.data.model.ReporteRequest
import com.delgado.reportesapp.data.repository.ReporteRepository
import com.delgado.reportesapp.databinding.ActivityRegistroReporteBinding
import com.delgado.reportesapp.utils.Resource
import kotlinx.coroutines.launch

class RegistroReporteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroReporteBinding
    private val repository = ReporteRepository()

    private val tiposBasura = arrayOf("DOMICILIARIA", "DESMONTE", "MALEZA", "OTROS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupDropdownTipo()
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

    private fun setupDropdownTipo() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposBasura)
        binding.actvTipo.setAdapter(adapter)
        // Valor por defecto
        binding.actvTipo.setText(tiposBasura[0], false)
    }

    private fun setupListeners() {
        binding.btnRegistrar.setOnClickListener {
            validarYRegistrar()
        }
    }

    private fun validarYRegistrar() {
        val dni = binding.etDni.text.toString().trim()
        val tipo = binding.actvTipo.text.toString().trim()
        val direccion = binding.etDireccion.text.toString().trim()
        val referencia = binding.etReferencia.text.toString().trim().ifBlank { null }

        // Limpiar errores previos
        binding.tilDni.error = null
        binding.tilDireccion.error = null
        binding.tilTipo.error = null

        var esValido = true

        // Validar DNI (exactamente 8 dígitos)
        if (dni.length != 8 || !dni.all { it.isDigit() }) {
            binding.tilDni.error = getString(R.string.error_dni_invalido)
            esValido = false
        }

        // Validar Dirección
        if (direccion.isBlank()) {
            binding.tilDireccion.error = getString(R.string.error_direccion_requerida)
            esValido = false
        }

        // Validar Tipo
        if (tipo.isBlank()) {
            binding.tilTipo.error = getString(R.string.error_tipo_requerido)
            esValido = false
        }

        if (!esValido) return

        // Crear DTO de petición sin id, fecha ni estado (el servidor Spring Boot los asigna)
        val request = ReporteRequest(
            dni = dni,
            tipo = tipo,
            direccion = direccion,
            referencia = referencia
        )

        enviarReporteApi(request)
    }

    private fun enviarReporteApi(request: ReporteRequest) {
        mostrarCargando(true)

        lifecycleScope.launch {
            val resultado = repository.registrarReporte(request)
            mostrarCargando(false)

            when (resultado) {
                is Resource.Success -> {
                    Toast.makeText(
                        this@RegistroReporteActivity,
                        R.string.msg_reporte_registrado,
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@RegistroReporteActivity,
                        resultado.message ?: getString(R.string.msg_error_conexion),
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Loading -> {
                    mostrarCargando(true)
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnRegistrar.isEnabled = !cargando
    }
}
