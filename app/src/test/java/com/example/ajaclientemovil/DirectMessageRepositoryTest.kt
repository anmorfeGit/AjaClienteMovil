package com.example.ajaclientemovil.repository

import android.content.Context
import com.example.ajaclientemovil.data.network.AjaApiService
import com.example.ajaclientemovil.network.SessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Clase de pruebas unitarias para [DirectMessageRepository].
 * * Se utiliza MockK para simular las dependencias de red (ApiService) y
 * el contexto de Android, permitiendo probar el funcionamiento de la clase
 * sin conexión a la bbdd.
 */
class DirectMessageRepositoryTest {

    // Mocks de las dependencias
    private val apiService = mockk<AjaApiService>()
    private val context = mockk<Context>(relaxed = true)

    private lateinit var repository: DirectMessageRepository

    @Before
    fun setUp() {
        // Inicializamos el repositorio
        repository = DirectMessageRepository(apiService, context)

        // Preparamos MockK para poder simular el objeto estático SessionManager
        mockkObject(SessionManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun SinTokenDevuelveSesionExpirada() = runTest {
        // El SessionManager devuelve null (no hay sesión)
        every { SessionManager.getToken(any()) } returns null

        // Intentamos enviar un mensaje
        val result = repository.sendMessage(1L, "Hola")

        // El resultado debe ser fallo y con el mensaje correcto
        assertTrue(result.isFailure)
        assertEquals("Sesión expirada", result.exceptionOrNull()?.message)
    }

    @Test
    fun SinTokenNoDevuelveConversaciones() = runTest {
        // No hay sesión
        every { SessionManager.getToken(any()) } returns null

        // Intentamos pedir las conversaciones
        val result = repository.fetchAllConversations()

        // Debe fallar con "Sin sesión"
        assertTrue(result.isFailure)
        assertEquals("Sin sesión", result.exceptionOrNull()?.message)
    }
}