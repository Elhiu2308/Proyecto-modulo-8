package com.ejemplo.fotosapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ejemplo.fotosapp.databinding.ItemFotoBinding
import com.ejemplo.fotosapp.models.Foto
import com.ejemplo.fotosapp.services.Utilidades

class FotoAdapter(
    private val fotos: List<Foto>,
    private val alSeleccionar: (Foto) -> Unit,
    private val alEliminar: (Foto) -> Unit
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    inner class FotoViewHolder(val binding: ItemFotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val binding = ItemFotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FotoViewHolder(binding)
    }

    override fun getItemCount(): Int = fotos.size

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]
        holder.binding.ivMiniatura.setImageBitmap(Utilidades.cargarBitmap(foto.ruta, 320))
        holder.binding.tvItemFecha.text = Utilidades.formatearFecha(foto.fecha)
        holder.binding.tvItemComentario.text = foto.comentario
        holder.binding.tvItemUbicacion.text = Utilidades.textoUbicacion(
            foto.latitud, foto.longitud
        )
        holder.binding.root.setOnClickListener { alSeleccionar(foto) }
        holder.binding.btnEliminar.setOnClickListener { alEliminar(foto) }
    }
}
