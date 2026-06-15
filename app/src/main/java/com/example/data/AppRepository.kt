package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(private val appDao: AppDao) {

    val allOrders: Flow<List<Order>> = appDao.getAllOrders()
    val allInventoryItems: Flow<List<InventoryItem>> = appDao.getAllInventoryItems()
    val lowStockItems: Flow<List<InventoryItem>> = appDao.getLowStockItems()

    fun getOrdersByPhone(phone: String): Flow<List<Order>> {
        return appDao.getOrdersByPhone(phone)
    }

    suspend fun getOrderById(id: Int): Order? {
        return appDao.getOrderById(id)
    }

    suspend fun insertOrder(order: Order): Long {
        return appDao.insertOrder(order)
    }

    suspend fun updateOrder(order: Order) {
        appDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: Order) {
        appDao.deleteOrder(order)
    }

    suspend fun insertInventoryItem(item: InventoryItem) {
        appDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        appDao.updateInventoryItem(item)
    }

    suspend fun updateStockLevel(itemId: Int, newStock: Int) {
        appDao.updateStockLevel(itemId, newStock)
    }

    fun getMessagesForOrder(orderId: Int): Flow<List<Message>> {
        return appDao.getMessagesForOrder(orderId)
    }

    suspend fun insertMessage(message: Message) {
        appDao.insertMessage(message)
    }

    // Prepopulate default shop supplies if none exist
    suspend fun prepopulateIfNeeded() {
        val items = allInventoryItems.first()
        if (items.isEmpty()) {
            val defaultItems = listOf(
                InventoryItem(nameEnglish = "Cyan Ink Premium", nameAmharic = "ሲያን ቀለም", category = "Ink", currentStock = 12, minimumStockLevel = 4, unit = "liters"),
                InventoryItem(nameEnglish = "Magenta Ink Premium", nameAmharic = "ማጄንታ ቀለም", category = "Ink", currentStock = 3, minimumStockLevel = 4, unit = "liters"), // Low stock!
                InventoryItem(nameEnglish = "Yellow Ink Premium", nameAmharic = "ቢጫ ቀለም", category = "Ink", currentStock = 8, minimumStockLevel = 3, unit = "liters"),
                InventoryItem(nameEnglish = "Black Ink Premium", nameAmharic = "ጥቁር ቀለም", category = "Ink", currentStock = 15, minimumStockLevel = 5, unit = "liters"),
                InventoryItem(nameEnglish = "Glossy Paper 300gsm A3+", nameAmharic = "ግሎሲ ወረቀት 300ግራም", category = "Paper", currentStock = 250, minimumStockLevel = 500, unit = "sheets"), // Low stock!
                InventoryItem(nameEnglish = "Flex Banner Roll 3.2m", nameAmharic = "ፍሌክስ ባነር ጥቅል", category = "Vinyl Roll", currentStock = 5, minimumStockLevel = 2, unit = "rolls"),
                InventoryItem(nameEnglish = "Matte Sticker Roll", nameAmharic = "ማቲ ስቲከር ጥቅል", category = "Vinyl Roll", currentStock = 1, minimumStockLevel = 2, unit = "rolls"), // Low stock!
                InventoryItem(nameEnglish = "Binding Wire Spiral", nameAmharic = "የስፒራል ሽቦ", category = "Accessories", currentStock = 120, minimumStockLevel = 30, unit = "pieces")
            )
            for (item in defaultItems) {
                appDao.insertInventoryItem(item)
            }

            // Create some default orders to instantly demonstrate features
            val defaultOrders = listOf(
                Order(
                    clientName = "Elias Abebe",
                    clientPhone = "0911223344",
                    orderTitle = "Sola Cafe Flyers",
                    orderCategory = "Posters/Flyers",
                    quantity = 1000,
                    totalPrice = 4500.0,
                    status = "Printing"
                ),
                Order(
                    clientName = "Hiwot Tesfaye",
                    clientPhone = "0922334455",
                    orderTitle = "Grand Opening Flex Banner",
                    orderCategory = "Banners",
                    quantity = 2,
                    totalPrice = 6200.0,
                    status = "Designing"
                ),
                Order(
                    clientName = "Solomon Kebede",
                    clientPhone = "0912345678",
                    orderTitle = "Business Cards Matte",
                    orderCategory = "Business Cards",
                    quantity = 500,
                    totalPrice = 1500.0,
                    status = "Finished" // Ready for pick up
                )
            )
            for (order in defaultOrders) {
                val orderId = appDao.insertOrder(order).toInt()
                // Add first initial welcoming system messages to orders
                appDao.insertMessage(Message(
                    orderId = orderId,
                    sender = "Production",
                    text = "Welcome to SOLA GRAPHICS! We have received your order request. Our design team is reviewing your requirements now."
                ))
            }
        }
    }
}
