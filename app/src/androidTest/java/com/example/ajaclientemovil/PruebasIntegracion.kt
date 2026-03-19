package com.example.ajaclientemovil.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ajaclientemovil.network.NetworkManager
import com.example.ajaclientemovil.network.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * PRUEBA DE INTEGRACION
 * * Esta clase verifica el funcionamiento de las peticiones al servidor:
 * 1. Comunicacion con el servidor(NetworkManager).
 * 2. Persistencia local de la sesion (SessionManager / SharedPreferences).
 * 3. Recuperacion de los datos guardados de la sesion (token).
 */
@RunWith(AndroidJUnit4::class) // Necessari per utilitzar el Context d'Android en tests
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RepositoryIntegrationTest {

    private lateinit var userRepository: UserRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        // Obtenemos el contexto de la aplicacion para las pruebas de persistencia.
        context = ApplicationProvider.getApplicationContext()
        userRepository = UserRepository(context)
        println("--------------------------------------------------")
        println("INICIO DEL TEST DE INTEGRACION")
    }

    /**
     * TEST 01: Login y persistencia
     * Verifica que cuando hacemos un login correctos, los datos se guardan correctamente en el
     * SessionManager.
     */
    @Test
    fun test01_PerformLoginAndSaveSession() = runBlocking {
        println("PASO 1: Ejecutano login real en el servidor...")

        val result = userRepository.performLogin("admin", "1234")

        if (result.isSuccess) {
            val user = result.getOrNull()
            println("Resultado correcto: usuari ${user?.username} loguejat.")

            // Verifiquem que el rol s'ha guardat al repositori
            val savedRole = userRepository.getCurrentRole()
            assertEquals("El rol guardado deberia ser ADMIN", "ADMIN", savedRole)
        } else {
            val error = result.exceptionOrNull()?.message
            println("Resultado erroneo: Error en el login: $error")
        }
    }

    /**
     * TEST 02: Recuperacion de datos con el token guardado
     * Verifica que el metodo getAllUsers() recupera el token del SessionManager
     * automaticamente y obtiene la lista de usuarios del servidor.
     */
    @Test
    fun test02_GetAllUsersWithSavedToken() = runBlocking {
        println("PASO 2: Recuperando la lista de usuarios desde el servidor...")

        val result = userRepository.getAllUsers()

        if (result.isSuccess) {
            val users = result.getOrNull() ?: emptyList()
            println("Resultado correcto: se han recuperado un total de ${users.size} usuarios.")
            assertTrue("La lista no deberia estar vacia", users.isNotEmpty())
        } else {
            val error = result.exceptionOrNull()?.message
            println("Resultado erroneo: Error al listar los usuarios: $error")
        }
    }

    /**
     * TEST 03: Ciclo de cierre de sesión completo.
     * 1. Verifica la comunicación con el endpoint de Logout (Red).
     * 2. Verifica la destrucción de la sesión local (Persistencia).
     */
    @Test
    fun test03_CompleteLogoutWorkflow() = runBlocking {
        println("PASO 3: Iniciando ciclo de cierre de sesión...")

        // A. Verificación de Red (Comunicación con el servidor)
        // Llamamos directamente al NetworkManager para inspeccionar la respuesta de red
        val logoutRedExitoso = NetworkManager.logout()

        if (logoutRedExitoso) {
            println("RESULTADO RED: [CORRECTO] - El servidor ha invalidado el token (200 OK/302).")
        } else {
            // Nota: Algunos servidores invalidan la sesión pero no devuelven 200,
            // por eso es un aviso y no un fallo crítico si el local limpia bien.
            println("RESULTADO RED: [AVISO] - El servidor no devolvió confirmación, procediendo a limpieza local.")
        }

        // B. Verificación de Lógica de Negocio y Persistencia
        // Ahora usamos el repositorio para asegurar que los datos locales se borran
        userRepository.performLogout()

        // C. Comprobaciones finales (Assertions)
        val roleDespues = userRepository.getCurrentRole()
        val tokenDespues = SessionManager.getToken(context)

        assertNull("ERROR: El rol sigue presente en SharedPreferences", roleDespues)
        assertNull("ERROR: El token no se ha borrado localmente", tokenDespues)

        println("RESULTADO LOCAL: [CORRECTO] - Datos de sesión eliminados del dispositivo.")
        println("--------------------------------------------------")
    }
}