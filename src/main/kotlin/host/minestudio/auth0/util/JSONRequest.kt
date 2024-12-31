package host.minestudio.auth0.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.HashMap

/**
 * JSONRequest is a utility class that is used
 * to send HTTP requests to a server and
 * receive a JSON response.
 */
class JSONRequest private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        /**
         * Sends an HTTP request to a server and returns a JSON response.
         * @param url The URL to send the request to
         * @param requestMethod The HTTP request method to use
         * @param requestProperties The request properties to send
         * @param includeBody Whether to include a body in the request
         * @param body The body to send with the request
         * @return A [JsonObject] containing the response
         * @throws IOException If an error occurs while sending the request
         */
        @Throws(IOException::class)
        fun request(
            url: String,
            requestMethod: String?,
            requestProperties: HashMap<String, String>,
            includeBody: Boolean,
            body: ByteArray?
        ): JsonObject {
            try {
                val requestUrl = URI(url).toURL()
                val con = requestUrl.openConnection() as HttpURLConnection
                con.requestMethod = requestMethod
                con.doInput = true
                con.doOutput = true
                con.useCaches = false
                requestProperties.forEach { (key: String?, value: String?) -> con.setRequestProperty(key, value) }
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                con.connect()
                if (java.lang.Boolean.TRUE == includeBody) {
                    con.outputStream.use { os ->
                        if (body != null) {
                            os.write(body, 0, body.size)
                        }
                    }
                    val inputStream = if (con.responseCode !in 200..299) {
                        con.errorStream ?: throw IOException("Error stream is null for response code: ${con.responseCode}")
                    } else {
                        con.inputStream ?: throw IOException("Input stream is null for response code: ${con.responseCode}")
                    }
                    return parseJSON(inputStream)
                }
                val response = StringBuilder()
                val conScanner = Scanner(con.inputStream)
                while (conScanner.hasNext()) {
                    response.append(conScanner.nextLine())
                }

                return JsonParser.parseString(response.toString()).asJsonObject
            } catch (x: ConnectException) {
                return JsonParser.parseString("{\"success\": false}").asJsonObject
            } catch (e: URISyntaxException) {
                throw RuntimeException("Invalid URL: $url")
            }
        }

        /**
         * Parses a JSON response from an [InputStream].
         * @param inputStream The [InputStream] to parse
         * @return A [JsonObject] containing the response
         */
        protected fun parseJSON(inputStream: InputStream): JsonObject {
            try {
                BufferedReader(
                    InputStreamReader(inputStream, StandardCharsets.UTF_8)
                ).use { br ->
                    val response = StringBuilder()
                    var responseLine: String
                    while ((br.readLine().also { responseLine = it }) != null) {
                        response.append(responseLine.trim { it <= ' ' })
                    }
                    return JsonParser.parseString(response.toString()).asJsonObject
                }
            } catch (x: Exception) {
                return JsonParser.parseString("{\"success\": false}").asJsonObject
            }
        }
    }
}
