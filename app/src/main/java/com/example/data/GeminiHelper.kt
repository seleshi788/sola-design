package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private const val TAG = "GeminiHelper"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun escapeJsonString(str: String): String {
        val builder = StringBuilder()
        for (i in 0 until str.length) {
            val c = str[i]
            when (c) {
                '\\' -> builder.append("\\\\")
                '"' -> builder.append("\\\"")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> {
                    if (c.code < 32) {
                        val hex = Integer.toHexString(c.code)
                        builder.append("\\u").append("0000".substring(hex.length)).append(hex)
                    } else {
                        builder.append(c)
                    }
                }
            }
        }
        return builder.toString()
    }

    suspend fun getProductionAssistantReply(
        clientMessage: String,
        orderTitle: String,
        orderStatus: String,
        languageAmharic: Boolean
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local responder.")
            return@withContext getLocalFallbackReply(clientMessage, orderTitle, orderStatus, languageAmharic)
        }

        val prompt = if (languageAmharic) {
            """
            እርስዎ የ "SOLA GRAPHICS DESIGN & ADVERT" ህትመት ድርጅት የምርት ረዳት ነዎት።
            ደንበኛው የእርስዎን እገዛ ይጠይቃል።
            የትዕዛዝ ስም: "$orderTitle"
            በትዕዛዝ ሁኔታ: "$orderStatus"
            የደንበኛ መልዕክት: "$clientMessage"
            እባክዎ በአማርኛ ቋንቋ አጭር፣ ተግባቢ፣ እና ቀጥተኛ ምላሽ ይፃፉ። ለምሳሌ የሚቀጥለውን የህትመት ሂደት ወይም ዲዛይን ሁኔታ ያብራሩ። ከ2 እስከ 3 ዓረፍተ ነገሮች ይበቃል።
            """.trimIndent()
        } else {
            """
            You are the Production Assistant at "SOLA GRAPHICS DESIGN & ADVERT" printing press.
            A client is asking about their order.
            Order Title: "$orderTitle"
            Current order status: "$orderStatus"
            Client Message: "$clientMessage"
            Please write a friendly, helpful, and concise response as the Sola production team, explaining what steps are being taken for their "$orderTitle" print order. Limit response to 2 or 3 sentences.
            """.trimIndent()
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val escapedPrompt = escapeJsonString(prompt)
        val requestJsonString = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "$escapedPrompt"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val requestBody = requestJsonString.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code: ${response.code}")
                    return@withContext getLocalFallbackReply(clientMessage, orderTitle, orderStatus, languageAmharic)
                }
                val bodyString = response.body?.string() ?: ""
                
                // Extracting text simply from response json
                // Format: {"candidates": [{"content": {"parts": [{"text": "..."}]}}]}
                val textResponse = extractTextFromJson(bodyString)
                if (textResponse != null) {
                    return@withContext textResponse
                } else {
                    return@withContext getLocalFallbackReply(clientMessage, orderTitle, orderStatus, languageAmharic)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            return@withContext getLocalFallbackReply(clientMessage, orderTitle, orderStatus, languageAmharic)
        }
    }

    private fun extractTextFromJson(jsonStr: String): String? {
        return try {
            val candidateStart = jsonStr.indexOf("\"text\":")
            if (candidateStart != -1) {
                val startQuote = jsonStr.indexOf("\"", candidateStart + 7)
                var endQuote = startQuote + 1
                while (endQuote < jsonStr.length) {
                    if (jsonStr[endQuote] == '"' && jsonStr[endQuote - 1] != '\\') {
                        break
                    }
                    endQuote++
                }
                if (startQuote != -1 && endQuote != -1) {
                    val rawText = jsonStr.substring(startQuote + 1, endQuote)
                    // Unescape newlines / quotes
                    return rawText
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getLocalFallbackReply(
        clientMessage: String,
        orderTitle: String,
        orderStatus: String,
        languageAmharic: Boolean
    ): String {
        val lowercaseMsg = clientMessage.lowercase()
        return if (languageAmharic) {
            when {
                lowercaseMsg.contains("ሰላም") || lowercaseMsg.contains("ጤና") -> 
                    "ሰላም! ስለ $orderTitle ትዕዛዝዎ ስላነጋገሩን እናመሰግናለን። በአሁኑ ጊዜ ትዕዛዝዎ በ'$orderStatus' ደረጃ ላይ ነው። ማንኛውንም ጥያቄ እዚህ መጠየቅ ይችላሉ።"
                lowercaseMsg.contains("መቼ") || lowercaseMsg.contains("ቀን") || lowercaseMsg.contains("አበቃ") -> 
                    "የትዕዛዝዎ ($orderTitle) ህትመት ፍጥነት በአሁኑ ጊዜ በጥሩ ሁኔታ ላይ ነው። በቅርቡ ዝግጁ ስናደርግ አውቶማቲክ መልእክት እንልክልዎታለን።"
                lowercaseMsg.contains("ዋጋ") || lowercaseMsg.contains("ብር") -> 
                    "የዚህ ትዕዛዝ ጠቅላላ ዋጋ የተስተካከለ ሲሆን ቅድመ ክፍያውን እንዳጠናቀቁ ምርቱ በፍጥነት ይጀምራል።"
                else -> 
                    "ጤና ይስጥልኝ! ከ 'ሶላ ግራፊክስ' የምርት ቡድን ነው። ትዕዛዝዎ \"$orderTitle\" በአሁኑ ጊዜ በ\"$orderStatus\" ደረጃ ላይ ይገኛል። ፍላጎትዎን ለማሳካት እየሰራን ነው!"
            }
        } else {
            when {
                lowercaseMsg.contains("hello") || lowercaseMsg.contains("hi") -> 
                    "Hello there! Thank you for reaching out regarding your order '$orderTitle'. It is currently in the '$orderStatus' stage. Let us know how we can help!"
                lowercaseMsg.contains("when") || lowercaseMsg.contains("delivery") || lowercaseMsg.contains("ready") -> 
                    "Our production crew is actively processing '$orderTitle'. We are on track and will notify you immediately once it enters the 'Ready' stage!"
                lowercaseMsg.contains("price") || lowercaseMsg.contains("cost") || lowercaseMsg.contains("how much") -> 
                    "The price is confirmed. If you have made the deposit, production is initiated right away without delay."
                else -> 
                    "Hi! This is the Sola Graphics production crew. Your order '$orderTitle' is currently in the '$orderStatus' stage. We're fully focused on delivering the best quality!"
            }
        }
    }
}
