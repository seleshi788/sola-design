package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val clientPhone: String,
    val orderTitle: String,
    val orderCategory: String,
    val quantity: Int,
    val totalPrice: Double,
    val status: String, // "Submitted", "Accepted", "Designing", "Printing", "Finishing", "Ready", "Delivered"
    val submissionTime: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun getFormattedId(): String = "SOLA-${1000 + id}"
}

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameEnglish: String,
    val nameAmharic: String,
    val category: String, // "Paper", "Ink", "Vinyl Roll", "Banners", "Accessories"
    val currentStock: Int,
    val minimumStockLevel: Int,
    val unit: String // "packs", "liters", "rolls", "pieces"
) {
    fun isLowStock(): Boolean = currentStock <= minimumStockLevel
}

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int, // associated printing order
    val sender: String, // "Client" or "Production"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
