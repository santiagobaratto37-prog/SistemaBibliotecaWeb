package com.mycompany.sistemabibliotecaweb;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import jakarta.servlet.RequestDispatcher;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LibroServletTest {

    private LibroServlet servlet;
    private librosDAO dao;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter salida;
    private PrintWriter escritor;

    @Before
    public void preparar() throws Exception {
        servlet = new LibroServlet();
        dao = mock(librosDAO.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        Field campo = LibroServlet.class.getDeclaredField("dao");
        campo.setAccessible(true);
        campo.set(servlet, dao);

        salida = new StringWriter();
        escritor = new PrintWriter(salida);

        when(response.getWriter()).thenReturn(escritor);
        when(request.getContextPath())
                .thenReturn("/SistemaBibliotecaWeb");
    }

    private void prepararDatos(String accion) {
        when(request.getParameter("accion")).thenReturn(accion);
        when(request.getParameter("id")).thenReturn("25");
        when(request.getParameter("titulo")).thenReturn("Prueba GA8");
        when(request.getParameter("autor")).thenReturn("Autor de prueba");
        when(request.getParameter("stock")).thenReturn("5");
        when(request.getParameter("idCategoria")).thenReturn("1");
    }

    @Test
    public void registrarLibroEnviaDatosAlDAO() throws Exception {
        prepararDatos("insertar");
        when(dao.insertar(any(libro.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).insertar(argThat(l ->
                "Prueba GA8".equals(l.getTitulo())
                && "Autor de prueba".equals(l.getAutor())
                && l.getStock() == 5
                && l.getIdCategoria() == 1));

        verify(response).sendRedirect(
                "/SistemaBibliotecaWeb/LibroServlet?resultado=true");
    }

    @Test
    public void actualizarLibroEnviaIdYDatosAlDAO() throws Exception {
        prepararDatos("actualizar");
        when(dao.actualizar(any(libro.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).actualizar(argThat(l ->
                l.getIdLibro() == 25
                && "Prueba GA8".equals(l.getTitulo())
                && "Autor de prueba".equals(l.getAutor())
                && l.getStock() == 5
                && l.getIdCategoria() == 1));

        verify(response).sendRedirect(
                "/SistemaBibliotecaWeb/LibroServlet?resultado=true");
    }

    @Test
    public void eliminarLibroEnviaIdAlDAO() throws Exception {
        prepararDatos("eliminar");
        when(dao.eliminar(25)).thenReturn(true);

        servlet.doPost(request, response);

        verify(dao).eliminar(25);
        verify(response).sendRedirect(
                "/SistemaBibliotecaWeb/LibroServlet?resultado=true");
    }

    @Test
    public void stockNoNumericoDevuelveError400() throws Exception {
        prepararDatos("insertar");
        when(request.getParameter("stock")).thenReturn("abc");

        servlet.doPost(request, response);

        verify(response).sendError(eq(400), anyString());
     
        verifyNoInteractions(dao);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
public void consultarLibrosGeneraContenido() throws Exception {
    RequestDispatcher dispatcher = mock(RequestDispatcher.class);

    when(dao.listar()).thenReturn(Collections.emptyList());
    when(request.getRequestDispatcher(anyString()))
            .thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(dao).listar();
    verify(dispatcher).forward(request, response);
}
}