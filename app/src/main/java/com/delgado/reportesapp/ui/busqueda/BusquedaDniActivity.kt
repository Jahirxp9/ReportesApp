package com.delgado.reportesapp.ui.busqueda

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
import com.delgado.reportesapp.R
import com.delgado.reportesapp.data.model.Reporte
import com.delgado.reportesapp.data.repository.ReporteRepository
import com.delgado.reportesapp.databinding.ActivityBusquedaDniBinding
import com.delgado.reportesapp.ui.adapter.ReporteAdapter
import com.delgado.reportesapp.ui.detalle.DetalleReporteActivity
import com.delgado.reportesapp.utils.Constants
import com.delgado.reportesapp.utils.Resource
import kotlinx.coroutines.launch

class BusquedaDniActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusquedaDniBinding
    private val repository = ReporteRepository()
    private lateinit var adapter: ReporteAdapter
    private var ultimoDniBuscado: String = ""

    private val detalleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && ultimoDniBuscado.isNotBlank()) {
            ejecutarBusqueda(ultimoDniBuscado)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusquedaDniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupToolbar()
        setupRecyclerView()
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

    private fun setupRecyclerView() {
        adapter = ReporteAdapter { reporte ->
            abrirDetalle(reporte)
        }
        binding.rvReportesDni.layoutManager = LinearLayoutManager(this)
        binding.rvReportesDni.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBuscar.setOnClickListener {
            val dni = binding.etDniBusqueda.text.toString().trim()
            if (dni.length == 8 && dni.all { it.isDigit() }) {
                binding.tilDniBusqueda.error = null
                ejecutarBusqueda(dni)
            } else {
                binding.tilDniBusqueda.error = getString(R.string.error_dni_invalido)
            }
        }
    }

    private fun ejecutarBusqueda(dni: String) {
        ultimoDniBuscado = dni
        mostrarCargando(true)
        ocultarMensajeVacio()

        lifecycleScope.launch {
            val res = repository.getReportesByDni(dni)
            mostrarCargando(false)

            when (res) {
                is Resource.Success -> {
                    val lista = res.data ?: emptyList()
                    if (lista.isEmpty()) {
                        adapter.setReportes(emptyList())
                        mostrarMensajeVacio(getString(R.string.msg_busqueda_vacia))
                    } else {
                        ocultarMensajeVacio()
                        adapter.setReportes(lista)
                    }
                }
                is Resource.Error -> {
                    adapter.setReportes(emptyList())
                    mostrarMensajeVacio(res.message ?: getString(R.string.msg_error_conexion))
                }
                is Resource.Loading -> {
                    mostrarCargando(true)
                }
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
        binding.btnBuscar.isEnabled = !cargando
    }

    private fun mostrarMensajeVacio(mensaje: String) {
        binding.tvEmptyMessage.text = mensaje
        binding.layoutEmptyBusqueda.visibility = View.VISIBLE
    }

    private fun ocultarMensajeVacio() {
        binding.layoutEmptyBusqueda.visibility = View.GONE
    }
}
