package com.levilin.maskmap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levilin.maskmap.data.Feature
import com.levilin.maskmap.databinding.ItemRecyclerviewBinding

class MainAdapter(private val itemClickListener: ItemClickListener): RecyclerView.Adapter<MainAdapter.DefaultViewHolder>() {

    var maskDataList: List<Feature> = emptyList()
    set(value) {
        field = value
        // Refresh when data import
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val itemRecyclerviewBinding = ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DefaultViewHolder(itemRecyclerviewBinding)
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder.itemRecyclerviewBinding.textStoreName.text = maskDataList[position].properties.name
        holder.itemRecyclerviewBinding.textAdultMaskQuantity.text = maskDataList[position].properties.mask_adult.toString()
        holder.itemRecyclerviewBinding.textChildMaskQuantity.text = maskDataList[position].properties.mask_child.toString()

        holder.itemRecyclerviewBinding.cardInfoLayout.setOnClickListener {
            itemClickListener.onItemClickListener(maskDataList[position])
        }
    }

    override fun getItemCount(): Int {
        return maskDataList.size
    }

    interface ItemClickListener {
        fun onItemClickListener(data: Feature)
    }

    class DefaultViewHolder(val itemRecyclerviewBinding: ItemRecyclerviewBinding): RecyclerView.ViewHolder(itemRecyclerviewBinding.root)
}