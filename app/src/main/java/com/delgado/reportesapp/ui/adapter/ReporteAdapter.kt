package com.delgado.reportesapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.delgado.reportesapp.R
import com.delgado.reportesapp.data.model.Reporte
import com.delgado.reportesapp.databinding.ItemReporteBinding

/**
 * Adaptador de RecyclerView para mostrar las tarjetas de reportes de basura.
 */
class ReporteAdapter(
    private val onItemClick: (Reporte) -> Unit
) : RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder>() {

    private val reportes = mutableListOf<Reporte>()

    fun setReportes(nuevaLista: List<Reporte>) {
        reportes.clear()
        reportes.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun agregarReportes(listaAdicional: List<Reporte>) {
        val startPos = reportes.size
        reportes.addAll(listaAdicional)
        notifyItemRangeInserted(startPos, listaAdicional.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val binding = ItemReporteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReporteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        holder.bind(reportes[position])
    }

    override fun getItemCount(): Int = reportes.size

    inner class ReporteViewHolder(
        private val binding: ItemReporteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reporte: Reporte) {
            val context = itemView.context
            
            val idText = if (reporte.id != null) {
                context.getString(R.string.label_codigo_format, reporte.id)
            } else {
                "Reporte sin ID"
            }
            binding.tvId.text = idText
            binding.tvTipo.text = "(${reporte.tipo})"
            binding.tvDireccion.text = reporte.direccion
            binding.tvFecha.text = reporte.fechaFormateada
            binding.tvDni.text = context.getString(R.string.label_dni_format, reporte.dni)

            if (reporte.esPendiente) {
                binding.tvEstado.text = context.getString(R.string.estado_pendiente)
                binding.tvEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pendiente)
                binding.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.status_pendiente_text))
            } else {
                binding.tvEstado.text = context.getString(R.string.estado_atendido)
                binding.tvEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_atendido)
                binding.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.status_atendido_text))
            }

            binding.cardReporte.setOnClickListener {
                onItemClick(reporte)
            }
        }
    }
}
