package com.example.ajaclientemovil

import android.app.Application
import com.example.ajaclientemovil.ui.viewmodel.HomeViewModel
import com.example.ajaclientemovil.network.SessionManager
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Clase de pruebas unitarias para HomeViewModel.
 * Se centra en validar los permisos y la autoría de los mensajes del foro.
 */
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    // Mockeamos el contexto de la aplicación para poder instanciar el ViewModel
    private val context = mockk<Application>(relaxed = true)

    @Before
    fun setup() {
        // Simulamos el objeto SessionManager para controlar los datos de sesión (ID, Rol, etc.)
        mockkObject(SessionManager)
        viewModel = HomeViewModel(context)
    }

    // --- PRUEBAS PARA LA LÓGICA DE EDICIÓN (canEditPost) ---
    // Regla: Solo el autor original puede modificar el texto de un mensaje.

    @Test
    fun testPropietarioPuedeEditar() {
        // Simulamos que el usuario logueado tiene el ID 10
        every { SessionManager.getUser(any())?.id } returns 10L
        // El post pertenece al usuario con ID 10, por lo tanto, debe permitir la edición
        val result = viewModel.canEditPost(postUserId = 10L)
        assertTrue("El dueño debería poder editar su post", result)
    }

    @Test
    fun testAdministradorNoPuedeEditarSiNoPropietario() {
        // Simulamos que el usuario es ADMIN pero su ID personal es 1
        every { SessionManager.getRole(any()) } returns "ADMIN"
        every { SessionManager.getUser(any())?.id } returns 1L
        // El post es de otra persona (ID 99). Un Admin NO debe poder alterar el contenido original.
        val result = viewModel.canEditPost(postUserId = 99L)
        assertFalse("Admin NO debería editar posts ajenos", result)
    }

    @Test
    fun testUsuarioNoPuedeEditarSiNoPropietario() {
        // Simulamos un usuario normal (USER) con ID 1
        every { SessionManager.getRole(any()) } returns "USER"
        every { SessionManager.getUser(any())?.id } returns 1L
        // Intenta editar un post ajeno (ID 99). Debe denegarse.
        val result = viewModel.canEditPost(postUserId = 99L)
        assertFalse("Usuario normal NO debería editar posts ajenos", result)
    }

    // --- PRUEBAS PARA LA LÓGICA DE BORRADO (canDeletePost) ---
    // Regla: Un Admin puede borrar cualquier cosa (moderación), un User solo lo suyo.

    @Test
    fun testAdminPuedeBorrarCualquierPost() {
        // Simulamos rol ADMIN
        every { SessionManager.getRole(any()) } returns "ADMIN"
        every { SessionManager.getUser(any())?.id } returns 1L
        // Al ser Admin, debe poder borrar un post de terceros (ID 99) por moderación
        val result = viewModel.canDeletePost(postUserId = 99L)
        assertTrue("Admin debería poder borrar cualquier post", result)
    }

    @Test
    fun testUsuarioNormalPuedeBorrarSusPosts() {
        // Simulamos un usuario normal con ID 10
        every { SessionManager.getRole(any()) } returns "USER"
        every { SessionManager.getUser(any())?.id } returns 10L
        // El post pertenece al usuario con ID 10, por lo tanto, debe poder borrarlo
        val result = viewModel.canDeletePost(postUserId = 10L)
        assertTrue("Usuario normal debería borrar sus propios posts", result)
    }

    @Test
    fun testUsuarioNoPuedeBorrarPostsSiNoPropietario() {
        // Simulamos un usuario normal con ID 1
        every { SessionManager.getRole(any()) } returns "USER"
        every { SessionManager.getUser(any())?.id } returns 1L
        // Intenta borrar un post ajeno (ID 99). Debe denegarse.
        val result = viewModel.canDeletePost(postUserId = 99L)
        assertFalse("Usuario normal NO debería borrar posts ajenos", result)
    }
}