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

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val context = mockk<Application>(relaxed = true)

    @Before
    fun setup() {
        mockkObject(SessionManager)
        viewModel = HomeViewModel(context)
    }

    // --- PRUEBAS PARA canEditPost ---

    @Test
    fun testPropietarioPuedeEditar() {
        every { SessionManager.getUser(any())?.id } returns 10L
        val result = viewModel.canEditPost(postUserId = 10L)
        assertTrue("El dueño debería poder editar su post", result)
    }

    @Test
    fun testAdministradorNoPuedeEditarSiNoPropietario() {
        every { SessionManager.getRole(any()) } returns "ADMIN"
        every { SessionManager.getUser(any())?.id } returns 1L
        val result = viewModel.canEditPost(postUserId = 99L)
        assertFalse("Admin NO debería editar posts ajenos", result)
    }

    @Test
    fun testUsuarioNoPuedeEditarSiNoPropietario() {
        every { SessionManager.getRole(any()) } returns "USER"
        every { SessionManager.getUser(any())?.id } returns 1L
        val result = viewModel.canEditPost(postUserId = 99L)
        assertFalse("Usuario normal NO debería editar posts ajenos", result)
    }

    // --- PRUEBAS PARA canDeletePost ---

    @Test
    fun testAdminPuedeBorrarCualquierPost() {
        every { SessionManager.getRole(any()) } returns "ADMIN"
        every { SessionManager.getUser(any())?.id } returns 1L
        val result = viewModel.canDeletePost(postUserId = 99L)
        assertTrue("Admin debería poder borrar cualquier post", result)
    }

    @Test
    fun testUsuarioNormalPuedeBorrarSusPosts() {
        every { SessionManager.getRole(any()) } returns "USER"
        every { SessionManager.getUser(any())?.id } returns 10L
        val result = viewModel.canDeletePost(postUserId = 10L)
        assertTrue("Usuario normal debería borrar sus propios posts", result)
    }

    @Test
    fun testUsuarioNoPuedeBorrarPostsSiNoPropietario() {
        every { SessionManager.getRole(any()) } returns "USER"
        every { SessionManager.getUser(any())?.id } returns 1L
        val result = viewModel.canDeletePost(postUserId = 99L)
        assertFalse("Usuario normal NO debería borrar posts ajenos", result)
    }
}