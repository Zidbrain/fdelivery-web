package io.github.zidbrain.fdelivery.client

import io.github.zidbrain.fdelivery.auth.model.LoginResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set

private const val HOST_PORT = 8080

val String.onStatic: String
    get() = "${window.location.origin.replaceAfterLast(':', HOST_PORT.toString())}$this"

object ApiClient {

    val client = HttpClient {
        defaultRequest {
            host = window.location.hostname
            port = HOST_PORT
        }
        install(ContentNegotiation) {
            json()
        }
        WebSockets {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    private val _credentials =
        MutableStateFlow<LoginResponseDto?>(sessionStorage["creds"]?.let { Json.decodeFromString(it) })

    val credentialsFlow: StateFlow<LoginResponseDto?> = _credentials
    var credentials: LoginResponseDto?
        get() = _credentials.value
        set(value) {
            sessionStorage["creds"] = Json.encodeToString(value!!)
            _credentials.update { value }
        }

    fun clear() {
        sessionStorage.clear()
        _credentials.update { null }
    }

    suspend inline fun <reified T, reified Response> post(relativePath: String, model: T): Response =
        client.post(relativePath) {
            credentials?.jwt?.let {
                bearerAuth(it)
            }
            contentType(ContentType.Application.Json)
            setBody(model)
        }.body()

    suspend inline fun <reified Response> get(relativePath: String): Response = client.get(relativePath) {
        credentials?.jwt?.let {
            bearerAuth(it)
        }
    }.body()

    suspend inline fun <reified Response> postFormData(relativePath: String, formData: List<PartData>): Response =
        client.submitFormWithBinaryData(relativePath, formData) {
            credentials?.jwt?.let {
                bearerAuth(it)
            }
        }.body()

    suspend inline fun <reified T> put(relativePath: String, model: T) {
        client.put(relativePath) {
            credentials?.jwt?.let {
                bearerAuth(it)
            }
            contentType(ContentType.Application.Json)
            setBody(model)
        }
    }

    inline fun <reified Receive> webSocket(
        relativePath: String,
        launchIn: CoroutineScope
    ): Flow<Receive> {
        val flow = MutableSharedFlow<Receive>()
        launchIn.launch {
            client.webSocket(
                path = relativePath
            ) {
                credentials?.jwt?.let {
                    sendSerialized(it)
                }
                while (true)
                    flow.emit(receiveDeserialized())
            }
        }
        return flow
    }
}