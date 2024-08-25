package com.example.ihryskoscanner

import  android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val products: MutableList<Details>,
    private val onItemClick: (Details) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = products[position]
                    onItemClick(product) // Trigger the click callback
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.name
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateList(newProducts: List<Details>) {
        products.clear() // Clear the existing items
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}