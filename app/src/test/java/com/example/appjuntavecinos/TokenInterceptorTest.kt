package com.example.appjuntavecinos

import com.example.appjuntavecinos.data.remote.SesionToken
import com.example.appjuntavecinos.data.remote.TokenInterceptor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response

class TokenInterceptorTest : StringSpec({

    beforeTest { SesionToken.token = null }
    afterSpec { SesionToken.token = null }

    // Una cadena de OkHttp falsa: devuelve una respuesta con el código que se pida y guarda la petición que salió
    class Servidor(codigo: Int) {
        val salida = slot<Request>()
        val cadena = mockk<Interceptor.Chain>()

        init {
            val original = Request.Builder().url("http://localhost/reservas").build()
            every { cadena.request() } returns original
            every { cadena.proceed(capture(salida)) } answers {
                Response.Builder()
                    .request(salida.captured)
                    .protocol(Protocol.HTTP_1_1)
                    .code(codigo)
                    .message("respuesta de prueba")
                    .build()
            }
        }
    }

    "un 401 en una petición con sesión avisa que la sesión venció" {
        SesionToken.token = "token-valido"
        var avisos = 0

        TokenInterceptor { avisos++ }.intercept(Servidor(401).cadena)

        avisos shouldBe 1
    }

    "un 401 sin sesión es el login con la clave incorrecta y no avisa" {
        var avisos = 0

        TokenInterceptor { avisos++ }.intercept(Servidor(401).cadena)

        avisos shouldBe 0
    }

    "una respuesta correcta con sesión no avisa" {
        SesionToken.token = "token-valido"
        var avisos = 0

        TokenInterceptor { avisos++ }.intercept(Servidor(200).cadena)

        avisos shouldBe 0
    }

    "un 403 es falta de permiso, no sesión vencida" {
        SesionToken.token = "token-valido"
        var avisos = 0

        TokenInterceptor { avisos++ }.intercept(Servidor(403).cadena)

        avisos shouldBe 0
    }

    "con sesión la petición sale con el token" {
        SesionToken.token = "token-valido"
        val servidor = Servidor(200)

        TokenInterceptor { }.intercept(servidor.cadena)

        servidor.salida.captured.header("Authorization") shouldBe "Bearer token-valido"
    }

    "sin sesión la petición sale sin Authorization" {
        val servidor = Servidor(200)

        TokenInterceptor { }.intercept(servidor.cadena)

        servidor.salida.captured.header("Authorization") shouldBe null
    }
})
