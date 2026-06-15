package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppLanguage { ENGLISH, AMHARIC }

enum class SolaTab { TRACK, NEW_ORDER, INVENTORY, SOCIALS }

class SolaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // Global states
    val language = MutableStateFlow(AppLanguage.ENGLISH)
    val activeTab = MutableStateFlow(SolaTab.TRACK)
    val isAdminMode = MutableStateFlow(false)

    // Search query for order tracking
    val trackingSearchQuery = MutableStateFlow("")

    // Active order selected for details and messaging list
    val selectedOrderId = MutableStateFlow<Int?>(null)

    // Flow states
    val allOrders: StateFlow<List<Order>>
    val allInventoryItems: StateFlow<List<InventoryItem>>
    val lowStockItems: StateFlow<List<InventoryItem>>

    // Combined or filtered orders for tracking search results
    val searchResults: StateFlow<List<Order>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Ensure database has default items on first launch
        viewModelScope.launch {
            repository.prepopulateIfNeeded()
        }

        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allInventoryItems = repository.allInventoryItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        lowStockItems = repository.lowStockItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Filter orders reactively based on tracking search query
        searchResults = combine(allOrders, trackingSearchQuery) { orders, query ->
            if (query.isBlank()) {
                orders
            } else {
                orders.filter { order ->
                    order.clientPhone.contains(query, ignoreCase = true) ||
                    order.getFormattedId().contains(query, ignoreCase = true) ||
                    order.clientName.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Dynamic messages flow for whichever order is active
    val activeMessages: StateFlow<List<Message>> = selectedOrderId.flatMapLatest { orderId ->
        if (orderId != null) {
            repository.getMessagesForOrder(orderId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Language Toggle helper
    fun toggleLanguage() {
        language.value = if (language.value == AppLanguage.ENGLISH) AppLanguage.AMHARIC else AppLanguage.ENGLISH
    }

    // Selection helper
    fun selectOrder(orderId: Int?) {
        selectedOrderId.value = orderId
    }

    // --- Actions ---

    // Submit new Order (Client side)
    fun createOrder(
        clientName: String,
        clientPhone: String,
        title: String,
        category: String,
        quantity: Int,
        totalPrice: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val order = Order(
                clientName = clientName,
                clientPhone = clientPhone,
                orderTitle = title,
                orderCategory = category,
                quantity = quantity,
                totalPrice = totalPrice,
                status = "Submitted",
                notes = notes
            )
            val orderId = repository.insertOrder(order).toInt()

            // System log message
            repository.insertMessage(Message(
                orderId = orderId,
                sender = "Production",
                text = "Order submitted successfully! Our staff will review and accept it shortly."
            ))
            
            // Auto select this newly created order to show details & tracking instantly
            selectedOrderId.value = orderId
            activeTab.value = SolaTab.TRACK
        }
    }

    // Accept Order (Production Staff)
    fun acceptOrder(order: Order) {
        viewModelScope.launch {
            val updated = order.copy(status = "Accepted")
            repository.updateOrder(updated)
            repository.insertMessage(Message(
                orderId = order.id,
                sender = "Production",
                text = "Your order has been officially accepted by SOLA GRAPHICS. We are preparing resources."
            ))
        }
    }

    // Advance Production Status (Production Staff Step-by-Step)
    fun advanceProductionStatus(order: Order) {
        val nextStatus = when (order.status) {
            "Submitted" -> "Accepted"
            "Accepted" -> "Designing"
            "Designing" -> "Printing"
            "Printing" -> "Finishing"
            "Finishing" -> "Ready"
            "Ready" -> "Delivered"
            else -> "Delivered"
        }

        viewModelScope.launch {
            val updated = order.copy(status = nextStatus)
            repository.updateOrder(updated)

            val statusMsg = when (nextStatus) {
                "Designing" -> "Creative designers are working on your design templates now."
                "Printing" -> {
                    // Reduce ink and paper stock when printing! Extremely neat simulation.
                    simulateStockConsumptionForCategory(order.orderCategory, order.quantity)
                    "Design approved! Your order is now on the printing press running production."
                }
                "Finishing" -> "Printing completed. Currently finishing, trimming, and spiral-binding."
                "Ready" -> "Quality check passed! Your printed items are fully ready for pickup at our office."
                "Delivered" -> "Order successfully picked up. Thank you for choosing SOLA GRAPHICS & ADVERT!"
                else -> "Status updated to $nextStatus"
            }

            repository.insertMessage(Message(
                orderId = order.id,
                sender = "Production",
                text = statusMsg
            ))
        }
    }

    // Cancel / Delete Order
    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            if (selectedOrderId.value == order.id) {
                selectedOrderId.value = null
            }
        }
    }

    // Update Stock quantity directly
    fun updateStock(itemId: Int, quantityChange: Int) {
        viewModelScope.launch {
            val items = allInventoryItems.value
            val target = items.find { it.id == itemId } ?: return@launch
            val nextStock = (target.currentStock + quantityChange).coerceAtLeast(0)
            repository.updateStockLevel(itemId, nextStock)
        }
    }

    // Client/Operator Chat message insertion
    fun sendMessage(text: String, isClientSender: Boolean, order: Order) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val senderLabel = if (isClientSender) "Client" else "Production"
            repository.insertMessage(Message(
                orderId = order.id,
                sender = senderLabel,
                text = text
            ))

            // If client sent a message and AI toggle helper is used, generate direct reply
            if (isClientSender) {
                // Get AI assistant response from GeminiHelper (REST or Mock fallback inside)
                viewModelScope.launch {
                    val amharicMode = (language.value == AppLanguage.AMHARIC)
                    val reply = GeminiHelper.getProductionAssistantReply(
                        clientMessage = text,
                        orderTitle = order.orderTitle,
                        orderStatus = order.status,
                        languageAmharic = amharicMode
                    )
                    repository.insertMessage(Message(
                        orderId = order.id,
                        sender = "Production",
                        text = reply
                    ))
                }
            }
        }
    }

    // Local print resource consumption automation helper
    private suspend fun simulateStockConsumptionForCategory(category: String, quantity: Int) {
        val items = allInventoryItems.value
        when (category) {
            "Business Cards" -> {
                // Consumes glossy paper and inks
                consumeStockByName("Cyan Ink Premium", 1)
                consumeStockByName("Magenta Ink Premium", 1)
                consumeStockByName("Yellow Ink Premium", 1)
                consumeStockByName("Glossy Paper 300gsm A3+", (quantity / 20).coerceAtLeast(1))
            }
            "Banners" -> {
                consumeStockByName("Flex Banner Roll 3.2m", (quantity).coerceAtLeast(1))
                consumeStockByName("Cyan Ink Premium", 2)
                consumeStockByName("Yellow Ink Premium", 2)
            }
            "Posters/Flyers" -> {
                consumeStockByName("Glossy Paper 300gsm A3+", (quantity / 10).coerceAtLeast(1))
                consumeStockByName("Black Ink Premium", 2)
            }
            "Brochures" -> {
                consumeStockByName("Glossy Paper 300gsm A3+", (quantity / 5).coerceAtLeast(1))
                consumeStockByName("Cyan Ink Premium", 1)
                consumeStockByName("Magenta Ink Premium", 1)
                consumeStockByName("Yellow Ink Premium", 1)
            }
        }
    }

    private suspend fun consumeStockByName(name: String, amount: Int) {
        val target = allInventoryItems.value.find { it.nameEnglish == name }
        if (target != null) {
            val newQty = (target.currentStock - amount).coerceAtLeast(0)
            repository.updateStockLevel(target.id, newQty)
        }
    }
}
