package com.mycompany.sistemabibliotecaweb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet({"/api/prestamos", "/api/prestamos/*"})
public class PrestamosApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!esListado(request)) {
            responder(response, 404, "error", "Ruta no encontrada");
            return;
        }

        String sql = "SELECT id_prestamo, id_usuario, id_libro, fecha_prestamo, "
                + "fecha_devolucion FROM prestamos ORDER BY id_prestamo";
        JsonArray prestamos = new JsonArray();

        try (Connection con = abrirConexion();
             PreparedStatement consulta = con.prepareStatement(sql);
             ResultSet filas = consulta.executeQuery()) {

            while (filas.next()) {
                JsonObject prestamo = new JsonObject();
                prestamo.addProperty("idPrestamo", filas.getInt("id_prestamo"));
                prestamo.addProperty("idUsuario",
                        filas.getObject("id_usuario", Integer.class));
                prestamo.addProperty("idLibro",
                        filas.getObject("id_libro", Integer.class));
                prestamo.addProperty("fechaPrestamo", filas.getString("fecha_prestamo"));
                prestamo.addProperty("fechaDevolucion", filas.getString("fecha_devolucion"));
                prestamos.add(prestamo);
            }
        } catch (SQLException e) {
            log("Error al consultar préstamos", e);
            responder(response, 500, "error", "No se pudieron consultar los préstamos");
            return;
        }

        prepararRespuesta(response, 200);
        gson.toJson(prestamos, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!esListado(request)) {
            responder(response, 404, "error", "Ruta no encontrada");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        PrestamoDatos datos;
        try {
            datos = gson.fromJson(request.getReader(), PrestamoDatos.class);
        } catch (JsonParseException e) {
            responder(response, 400, "error", "El JSON no es válido");
            return;
        }

        if (datos == null || datos.idUsuario <= 0 || datos.idLibro <= 0) {
            responder(response, 400, "error", "Indica idUsuario e idLibro válidos");
            return;
        }

        int idPrestamo;
        try (Connection con = abrirConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement usuario = con.prepareStatement(
                        "SELECT id_usuario FROM usuarios WHERE id_usuario = ?")) {
                    usuario.setInt(1, datos.idUsuario);
                    try (ResultSet filas = usuario.executeQuery()) {
                        if (!filas.next()) {
                            con.rollback();
                            responder(response, 404, "error", "Usuario no encontrado");
                            return;
                        }
                    }
                }

                // Descuenta una unidad únicamente cuando hay stock disponible.
                try (PreparedStatement stock = con.prepareStatement(
                        "UPDATE libros SET stock = stock - 1 WHERE id_libro = ? AND stock > 0")) {
                    stock.setInt(1, datos.idLibro);
                    if (stock.executeUpdate() == 0) {
                        con.rollback();
                        responder(response, 409, "error", "El libro no existe o no tiene stock");
                        return;
                    }
                }

                String sql = "INSERT INTO prestamos (id_usuario, id_libro, fecha_prestamo) "
                        + "VALUES (?, ?, CURRENT_DATE)";
                try (PreparedStatement insertar =
                        con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    insertar.setInt(1, datos.idUsuario);
                    insertar.setInt(2, datos.idLibro);
                    insertar.executeUpdate();

                    try (ResultSet claves = insertar.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("No se obtuvo el ID del préstamo");
                        }
                        idPrestamo = claves.getInt(1);
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log("Error al registrar préstamo", e);
            responder(response, 500, "error", "No se pudo registrar el préstamo");
            return;
        }

        JsonObject resultado = new JsonObject();
        resultado.addProperty("idPrestamo", idPrestamo);
        resultado.addProperty("mensaje", "Préstamo registrado correctamente");
        prepararRespuesta(response, 201);
        gson.toJson(resultado, response.getWriter());
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String ruta = request.getPathInfo();
        if (ruta == null || !ruta.matches("/[0-9]+/devolucion")) {
            responder(response, 404, "error", "Usa /api/prestamos/{id}/devolucion");
            return;
        }

        int idPrestamo;
        try {
            idPrestamo = Integer.parseInt(ruta.split("/")[1]);
            if (idPrestamo <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            responder(response, 400, "error", "El ID del préstamo no es válido");
            return;
        }

        try (Connection con = abrirConexion()) {
            con.setAutoCommit(false);
            try {
                Integer idLibro;

                // Bloquea el préstamo hasta terminar para evitar devolverlo dos veces.
                try (PreparedStatement consulta = con.prepareStatement(
                        "SELECT id_libro, fecha_devolucion FROM prestamos "
                        + "WHERE id_prestamo = ? FOR UPDATE")) {
                    consulta.setInt(1, idPrestamo);
                    try (ResultSet filas = consulta.executeQuery()) {
                        if (!filas.next()) {
                            con.rollback();
                            responder(response, 404, "error", "Préstamo no encontrado");
                            return;
                        }
                        if (filas.getDate("fecha_devolucion") != null) {
                            con.rollback();
                            responder(response, 409, "error", "El préstamo ya fue devuelto");
                            return;
                        }
                        idLibro = filas.getObject("id_libro", Integer.class);
                    }
                }

                if (idLibro == null) {
                    con.rollback();
                    responder(response, 409, "error", "El préstamo no tiene un libro asociado");
                    return;
                }

                try (PreparedStatement stock = con.prepareStatement(
                        "UPDATE libros SET stock = stock + 1 WHERE id_libro = ?")) {
                    stock.setInt(1, idLibro);
                    if (stock.executeUpdate() != 1) {
                        con.rollback();
                        responder(response, 409, "error", "El libro asociado no existe");
                        return;
                    }
                }

                try (PreparedStatement devolver = con.prepareStatement(
                        "UPDATE prestamos SET fecha_devolucion = CURRENT_DATE "
                        + "WHERE id_prestamo = ?")) {
                    devolver.setInt(1, idPrestamo);
                    devolver.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log("Error al registrar devolución", e);
            responder(response, 500, "error", "No se pudo registrar la devolución");
            return;
        }

        responder(response, 200, "mensaje", "Devolución registrada correctamente");
    }

    private boolean esListado(HttpServletRequest request) {
        String ruta = request.getPathInfo();
        return ruta == null || ruta.equals("/");
    }

    private Connection abrirConexion() throws SQLException {
        Connection con = conexion.conectar();
        if (con == null) {
            throw new SQLException("No se pudo conectar a MariaDB");
        }
        return con;
    }

    private void prepararRespuesta(HttpServletResponse response, int estado) {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void responder(HttpServletResponse response, int estado,
            String campo, String mensaje) throws IOException {
        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty(campo, mensaje);
        prepararRespuesta(response, estado);
        gson.toJson(cuerpo, response.getWriter());
    }

    private static class PrestamoDatos {
        private int idUsuario;
        private int idLibro;
    }
}