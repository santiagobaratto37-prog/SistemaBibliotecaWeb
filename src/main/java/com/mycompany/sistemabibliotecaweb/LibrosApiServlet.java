package com.mycompany.sistemabibliotecaweb;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/api/libros", "/api/libros/*"})
public class LibrosApiServlet extends HttpServlet {

    private final librosDAO dao = new librosDAO();
    private final Gson gson = new Gson();

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String ruta = request.getPathInfo();

    // Sin ID se devuelve el listado completo.
    if (ruta == null || ruta.equals("/")) {
        gson.toJson(dao.listar(), response.getWriter());
        return;
    }

    try {
        int id = Integer.parseInt(ruta.substring(1));
        if (id <= 0) {
            throw new NumberFormatException();
        }

        libro encontrado = dao.buscarPorId(id);
        if (encontrado == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Libro no encontrado\"}");
            return;
        }

        gson.toJson(encontrado, response.getWriter());
    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"El ID debe ser un entero positivo\"}");
    } catch (java.sql.SQLException e) {
        log("Error al consultar el libro", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("{\"error\":\"No se pudo consultar el libro\"}");
    }
}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Convierte el JSON enviado por Postman en un objeto libro.
        libro nuevo;
        try {
            nuevo = gson.fromJson(request.getReader(), libro.class);
        } catch (JsonParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"El JSON no es válido\"}");
            return;
        }

        // Valida los datos antes de intentar guardar el libro.
        if (nuevo == null || nuevo.getTitulo() == null
                || nuevo.getTitulo().isBlank()
                || nuevo.getTitulo().length() > 150
                || (nuevo.getAutor() != null && nuevo.getAutor().length() > 100)
                || nuevo.getStock() < 0 || nuevo.getIdCategoria() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"error\":\"Revisa el título, el autor, el stock y la categoría\"}");
            return;
        }

        // Guarda el libro usando el DAO del proyecto.
        if (!dao.insertar(nuevo)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"No se pudo guardar el libro\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("{\"mensaje\":\"Libro registrado correctamente\"}");
    }
    @Override
protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
    modificarLibro(request, response, false);
}

@Override
protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
    modificarLibro(request, response, true);
}

private void modificarLibro(HttpServletRequest request,
        HttpServletResponse response, boolean eliminar) throws IOException {

    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String ruta = request.getPathInfo();
    int id;

    try {
        if (ruta == null || !ruta.matches("/[0-9]+")) {
            throw new NumberFormatException();
        }
        id = Integer.parseInt(ruta.substring(1));
        if (id <= 0) {
            throw new NumberFormatException();
        }
    } catch (NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"Indica un ID válido en la URL\"}");
        return;
    }

    try {
        if (dao.buscarPorId(id) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Libro no encontrado\"}");
            return;
        }

        if (eliminar) {
            if (!dao.eliminar(id)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"No se pudo eliminar el libro\"}");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        com.google.gson.JsonObject datos =
                gson.fromJson(request.getReader(), com.google.gson.JsonObject.class);

        if (datos == null || !datos.has("stock") || datos.get("stock").isJsonNull()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"El stock es obligatorio\"}");
            return;
        }

        libro actualizado = gson.fromJson(datos, libro.class);
        if (actualizado.getTitulo() == null || actualizado.getTitulo().isBlank()
                || actualizado.getTitulo().length() > 150
                || (actualizado.getAutor() != null && actualizado.getAutor().length() > 100)
                || actualizado.getStock() < 0 || actualizado.getIdCategoria() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"error\":\"Revisa el título, el autor, el stock y la categoría\"}");
            return;
        }

        // El ID de la URL identifica el libro que se actualiza.
        actualizado.setIdLibro(id);
        if (!dao.actualizar(actualizado)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"No se pudo actualizar el libro\"}");
            return;
        }

        response.getWriter().write("{\"mensaje\":\"Libro actualizado correctamente\"}");
    } catch (com.google.gson.JsonParseException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"El JSON no es válido\"}");
    } catch (java.sql.SQLException e) {
        log("Error al consultar el libro antes de modificarlo", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("{\"error\":\"No se pudo consultar el libro\"}");
    }
}
}