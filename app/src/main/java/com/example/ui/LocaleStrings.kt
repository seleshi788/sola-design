package com.example.ui

object LocaleStrings {
    private val translations = mapOf(
        "app_name" to Pair("SOLA GRAPHICS", "ሶላ ግራፊክስ"),
        "app_full_name" to Pair("SOLA GRAPHICS DESIGN & ADVERT", "ሶላ ግራፊክስ ዲዛይንና ማስታወቂያ"),
        "motto" to Pair("Design * Printing * Branding * Advert", "ዲዛይን * ህትመት * ብራንዲንግ * ማስታወቂያ"),
        "track_tab" to Pair("Order Tracking", "ትዕዛዝ መከታተያ"),
        "new_order_tab" to Pair("New Order", "አዲስ ትዕዛዝ"),
        "inventory_tab" to Pair("Supplies & Stock", "የእቃዎች ክምችት"),
        "socials_tab" to Pair("Social Links", "ማህበራዊ ሚዲያ"),
        "admin_title" to Pair("PRODUCTION MANAGEMENT", "የምርት አስተዳደር ማዕከል"),
        "admin_desc" to Pair("Simulation Panel: Process orders and update stock levels.", "የሲሙሌሽን መስኮት፡ ትዕዛዞችን ያከናውኑ እና ክምችት ያዘምኑ።"),
        "switch_to_admin" to Pair("Enter Staff Mode", "ወደ ሰራተኛ አስተዳደር ቀይር"),
        "switch_to_client" to Pair("Exit Staff Mode", "ወደ ደንበኛ ሁነታ ተመለስ"),
        "search_label" to Pair("Search Order Title, Phone, or ID", "በትዕዛዝ ስም፣ ስልክ ቁጥር ወይም መለያ ይፈልጉ"),
        "search_hint" to Pair("Type phone (e.g. 0911...) or ID (e.g. SOLA-1001)", "ስልክ (ለምሳሌ 0911...) ወይም ኮድ (SOLA-1001) ያስገቡ"),
        "order_details" to Pair("ORDER DETAILS", "የትዕዛዝ ዝርዝር መረጃ"),
        "client_name" to Pair("Client Name", "የደንበኛ ስም"),
        "client_phone" to Pair("Client Phone", "የደንበኛ ስልክ ቁጥር"),
        "order_title" to Pair("Printing Order Title", "የትዕዛዝ ስም / ርዕስ"),
        "category" to Pair("Category / Material", "የህትመት አይነት / ቁሳቁስ"),
        "quantity" to Pair("Quantity Needed", "ብዛት / ታይፒንግ"),
        "total_price" to Pair("Total Estimated Price", "ጠቅላላ የዋጋ ግምት"),
        "notes" to Pair("Design specs / Material notes", "የዲዛይን ወይም ቁሳቁስ ማሳሰቢያዎች"),
        "status" to Pair("Production Status", "የምርት ደረጃ"),
        "time" to Pair("Submission Date", "የገባበት ቀን"),
        "recent_messages" to Pair("Production Chat Room", "የምርት ቡድን የውይይት ክፍል"),
        "order_status_title" to Pair("Current Stage", "ያለበት ወቅታዊ ደረጃ"),
        "low_stock_warning" to Pair("LOW STOCK ALERT!", "ዝቅተኛ የክምችት ማስጠንቀቂያ!"),
        "low_stock_badge" to Pair("LOW", "ዝቅተኛ"),
        "inventory_list" to Pair("SUPPLIES INVENTORY", "የህትመት ጥሬ ዕቃዎች ክምችት"),
        "add_stock" to Pair("Refill Stock", "ክምችት ግዛ"),
        "messages_count" to Pair("Messages", "መልዕክቶች"),
        "submit_order" to Pair("Submit Order to Sola", "ትዕዛዙን ለሶላ ላክ"),
        "validation_error" to Pair("Please fill in Name, Phone, and Order Title!", "እባክዎ ስም፣ ስልክ ቁጥር እና የትዕዛዝ ርዕስ ያስገቡ!"),
        "ai_active" to Pair("Sola AI assistant will translate & reply instantly.", "የሶላ AI ረዳት ይተረጉማል እንዲሁም በቅጽበት ይመልሳል።"),
        "message_placeholder" to Pair("Ask the production team anything about your order...", "ስለ ትዕዛዝዎ የምርት ቡድኑን ማንኛውንም ነገር ይጠይቁ..."),
        "send_btn" to Pair("Send", "ላክ"),
        "current_stock" to Pair("Current Stock:", "ያለው ክምችት:"),
        "min_required" to Pair("Min Required:", "ዝቅተኛው ደህንነቱ የተጠበቀ መጠን:"),
        "unit" to Pair("Unit:", "መጠቅለያ:"),
        "category_label" to Pair("Category:", "የትየባ ምድብ:"),
        "no_orders_found" to Pair("No orders found matching your search.", "ከፍለጋዎ ጋር የሚዛመድ ትዕዛዝ አልተገኘም።"),
        "orders_count" to Pair("All Production Orders", "ጠቅላላ የምርት ትዕዛዞች"),
        "social_title" to Pair("CONNECT WITH SOLA GRAPHICS", "ከሶላ ግራፊክስ ጋር ይገናኙ"),
        "social_subtitle" to Pair("Explore our latest prints & get direct promos on Addis Ababa's best advert channels!", "አዳዲስ ስራዎችን ለማየት እና ልዩ ቅናሾችን ለማግኘት በአዲስ አበባ ተወዳጅ ማህበራዊ ድረ-ገጾች ይገናኙን!"),
        "order_design" to Pair("Add design files & notes to get a premium customized high-end banner, business card, or wedding invitations.", "ለማስታወቂያ ባነሮች፣ ቢዝነስ ካርዶች ወይም ለሰርግ ካርድ ግሩም ዲዛይን ለማግኘት ልዩ ማሳሰቢያዎን እዚህ ያክሉ ።"),
        "services_title" to Pair("OUR PREMIUM IMAGING SERVICES", "የምናቀርባቸው ዋና ዋና የህትመት አገልግሎቶች"),
        "service_1" to Pair("High-Resolution Flex Banners & Stickers", "ጥራት ያላቸው የፍሌክስ ባነሮች እና ስቲከር ህትመቶች"),
        "service_2" to Pair("Premium Corporate Business Cards", "ለድርጅት የሚሆኑ ዘመናዊ የቢዝነስ ካርዶች"),
        "service_3" to Pair("Elegant Wedding & Event Invitation Cards", "ደስ የሚሉ የሰርግ እና የክስተቶች የግብዣ ካርዶች"),
        "service_4" to Pair("Bulk Flyers, Posters, & Promotional Booklets", "ፍላየሮች፣ ፖስተሮች እና የማስተዋወቂያ ቡክሌቶች በትልቅ ብዛት")
    )

    fun get(key: String, language: AppLanguage): String {
        val pair = translations[key] ?: return key
        return if (language == AppLanguage.ENGLISH) pair.first else pair.second
    }
}
