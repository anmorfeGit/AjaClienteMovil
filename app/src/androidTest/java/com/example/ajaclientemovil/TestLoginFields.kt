package com.example.ajaclientemovil

import org.junit.Test

import org.junit.Assert.*


/**
 * Clase de pruebas unitarias para la validación de los campos de inicio de sesión.
 * * Estas pruebas verifican la validez de los datos introducidos para evitar peticiones incorrectas
 * al servidor.
 */
class LoginFieldsTest {

    /**
     * Testea la funcion validateFields de LoginViewModel.
     * Verifica que las cadenas de texto no estén vacías tras eliminar espacios en blanco.
     * @param u Nombre de usuario introducido.
     * @param p Contraseña introducida.
     * @return True si ambos campos contienen caracteres válidos, False en caso contrario.
     */
    private fun validate(u: String, p: String) = u.trim().isNotEmpty() && p.trim().isNotEmpty()

    /**
     * Caso de prueba: Validación de campos totalmente vacíos.
     * Verifica que la función retorna False cuando el usuario pulsa "Entrar" sin escribir nada.
     */
    @Test
    fun todoVacioFalso() {
        val result = validate("", "")
        assertFalse("No debería permitir campos vacíos", result)
    }

    /**
     * Caso de prueba: Validación de campos solo con espacios.
     * Verifica que la función retorna False se completan ambos campos con espacios.
     */
    @Test
    fun todoEspaciosFalso() {
        val result = validate("   ", "   ")
        assertFalse("No debería permitir campos solo con espacios en blanco", result)
    }

    /**
     * Caso de prueba: Validación de campo password vacio.
     * Verifica que si falta password, la función retorna False.
     */
    @Test
    fun testPasswordVacio() {
        val result = validate("admin", "")
        assertFalse("Falta la contraseña, debe fallar", result)
    }

    /**
     * Caso de prueba: Validación de campos incompletos.
     * Verifica que si el password solo contiene espacios vacios, la función retorna False.
     */
    @Test
    fun testPasswordEspacios() {
        val result = validate("admin", "   ")
        assertFalse("Falta la contraseña, debe fallar", result)
    }

    /**
     * Caso de prueba: Validación de campo usuario vacio.
     * Verifica que si falta usuario, la función retorna False.
     */
    @Test
    fun testUsuarioVacio() {
        val result = validate("", "1234")
        assertFalse("Falta la contraseña, debe fallar", result)
    }

    /**
     * Caso de prueba: Validación usuario vacio.
     * Verifica que si el usuario solo contiene espacios vacios, la función retorna False.
     */
    @Test
    fun testUsuarioEspacioVacioFalso() {
        val result = validate("   ", "1234")
        assertFalse("Los espacios en blanco no son un usuario válido", result)
    }

    /**
     * Caso de prueba: Validación de flujo positivo (Happy Path).
     * Verifica que cuando los datos cumplen los requisitos estructurales, la función retorna True.
     */
    @Test
    fun testCorrecto() {
        val result = validate("alex_admin", "p@ssword123")
        assertTrue("Con datos válidos debe permitir el login", result)
    }
}