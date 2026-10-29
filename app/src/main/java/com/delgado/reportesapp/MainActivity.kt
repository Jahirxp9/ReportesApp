package com.delgado.reportesapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.delgado.reportesapp.data.model.Reporte
import com.delgado.reportesapp.data.repository.ReporteRepository
import com.delgado.reportesapp.databinding.ActivityMainBinding
import com.delgado.reportesapp.ui.adapter.ReporteAdapter
import com.delgado.reportesapp.ui.busqueda.BusquedaDniActivity
import com.delgado.reportesapp.ui.detalle.DetalleReporteActivity
import com.delgado.reportesapp.ui.registro.RegistroReporteActivity
import com.delgado.reportesapp.utils.Constants
import com.delgado.reportesapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Pantalla Principal: Listado de Reportes de Basura de la Municipalidad.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val repository = ReporteRepository()
    private lateinit var adapter: ReporteAdapter

    private var currentOffset = 0
    private val limit = Constants.DEFAULT_LIMIT
    private val reportesCargados = mutableListOf<Reporte>()

    // Launcher para refrescar lista al volver de registrar un nuevo reporte
    private val registroLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            cargarReportesIniciales()
        }
    }

    // Launcher para refrescar lista al volver de ver/atender un reporte
    private val detalleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            cargarReportesIniciales()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupRecyclerView()
        setupListeners()
        cargarReportesIniciales()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = ReporteAdapter { reporte ->
            abrirDetalle(reporte)
        }
        binding.rvReportes.layoutManager = LinearLayoutManager(this)
        binding.rvReportes.adapter = adapter
    }

    private fun setupListeners() {
        // Fab Nuevo Reporte
        binding.fabNuevoReporte.setOnClickListener {
            val intent = Intent(this, RegistroReporteActivity::class.java)
            registroLauncher.launch(intent)
        }

        // Botón Buscar por DNI
        binding.btnBuscarDni.setOnClickListener {
            val intent = Intent(this, BusquedaDniActivity::class.java)
            startActivity(intent)
        }

        // Botón Cargar 5 más
        binding.btnCargarMas.setOnClickListener {
            cargarMasReportes()
        }

        // Reintentar en estado de error o vacío
        binding.btnRetry.setOnClickListener {
            cargarReportesIniciales()
        }

        binding.btnRefreshEmpty.setOnClickListener {
            cargarReportesIniciales()
        }
    }

    private fun cargarReportesIniciales() {
        currentOffset = 0
        reportesCargados.clear()
        ocultarEstados()
        mostrarCargando(true)

        lifecycleScope.launch {
            val res = repository.getReportesPaginados(offset = currentOffset, limit = limit)
            mostrarCargando(false)

            when (res) {
                is Resource.Success -> {
                    val nuevos = res.data ?: emptyList()
                    if (nuevos.isEmpty()) {
                        mostrarEstadoVacio()
                    } else {
                        reportesCargados.addAll(nuevos)
                        val sinDuplicados = reportesCargados.distinctBy { it.id }
                        reportesCargados.clear()
                        reportesCargados.addAll(sinDuplicados)

                        adapter.setReportes(reportesCargados)

                        // Mostrar botón cargar más solo si devolvió la cantidad límite
                        binding.btnCargarMas.visibility = if (nuevos.size >= limit) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    mostrarEstadoError(res.message ?: getString(R.string.msg_error_conexion))
                }
                is Resource.Loading -> mostrarCargando(true)
            }
        }
    }

    private fun cargarMasReportes() {
        currentOffset += limit
        binding.btnCargarMas.isEnabled = false

        lifecycleScope.launch {
            val res = repository.getReportesPaginados(offset = currentOffset, limit = limit)
            binding.btnCargarMas.isEnabled = true

            when (res) {
                is Resource.Success -> {
                    val nuevos = res.data ?: emptyList()
                    if (nuevos.isNotEmpty()) {
                        val existentesIds = reportesCargados.mapNotNull { it.id }.toSet()
                        val realmenteNuevos = nuevos.filter { it.id !in existentesIds }
                        if (realmenteNuevos.isNotEmpty()) {
                            reportesCargados.addAll(realmenteNuevos)
                            adapter.agregarReportes(realmenteNuevos)
                        }
                    }
                    if (nuevos.size < limit) {
                        binding.btnCargarMas.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "No hay más reportes para cargar", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    currentOffset -= limit // Revertir incremento en caso de fallo
                    Toast.makeText(this@MainActivity, res.message ?: getString(R.string.msg_error_conexion), Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun abrirDetalle(reporte: Reporte) {
        val intent = Intent(this, DetalleReporteActivity::class.java).apply {
            putExtra(Constants.EXTRA_REPORTE, reporte)
        }
        detalleLauncher.launch(intent)
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
    }

    private fun ocultarEstados() {
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.btnCargarMas.visibility = View.GONE
    }

    private fun mostrarEstadoVacio() {
        ocultarEstados()
        adapter.setReportes(emptyList())
        binding.layoutEmpty.visibility = View.VISIBLE
    }

    private fun mostrarEstadoError(mensaje: String) {
        ocultarEstados()
        adapter.setReportes(emptyList())
        binding.tvErrorMessage.text = mensaje
        binding.layoutError.visibility = View.VISIBLE
    }
}
