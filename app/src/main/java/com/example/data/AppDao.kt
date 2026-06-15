package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY submissionTime DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): Order?

    @Query("SELECT * FROM orders WHERE clientPhone = :phone ORDER BY submissionTime DESC")
    fun getOrdersByPhone(phone: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    // --- Inventory ---
    @Query("SELECT * FROM inventory_items ORDER BY category ASC, nameEnglish ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE currentStock <= minimumStockLevel")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET currentStock = :newStock WHERE id = :itemId")
    suspend fun updateStockLevel(itemId: Int, newStock: Int)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getMessagesForOrder(orderId: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
}
