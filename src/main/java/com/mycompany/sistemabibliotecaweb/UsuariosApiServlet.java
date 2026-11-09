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

@WebServlet("/api/usuarios")
public class UsuariosApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String sql = "SELECT id_usuario, nombre, correo, telefono, tipo_usuario "
                + "FROM usuarios ORDER BY id_usuario";
        JsonArray usuarios = new JsonArray();

        try (Connection con = abrirConexion();
             PreparedStatement consulta = con.prepareStatement(sql);
             ResultSet filas = consulta.executeQuery()) {

            while (filas.next()) {
                JsonObject usuario = new JsonObject();
                usuario.addProperty("idUsuario", filas.getInt("id_usuario"));
                usuario.addProperty("nombre", filas.getString("nombre"));
                usuario.addProperty("correo", filas.getString("correo"));
                usuario.addProperty("telefono", filas.getString("telefono"));
                usuario.addProperty("tipoUsuario", filas.getString("tipo_usuario"));
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            log("Error al consultar los usuarios", e);
            responderError(response, 500, "No se pudieron consultar los usuarios");
            return;
        }

        responder(response, 200, usuarios);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        UsuarioDatos usuario;
        try {
            usuario = gson.fromJson(request.getReader(), UsuarioDatos.class);
        } catch (JsonParseException e) {
            responderError(response, 400, "El JSON no es válido");
            return;
        }

        // El registro exige nombre. Los demás campos son opcionales.
        if (usuario == null || usuario.nombre == null || usuario.nombre.isBlank()
                || excedeLongitud(usuario.nombre, 150)
                || excedeLongitud(usuario.correo, 150)
                || excedeLongitud(usuario.telefono, 50)
                || excedeLongitud(usuario.tipoUsuario, 50)) {
            responderError(response, 400, "Indica un nombre y revisa la longitud de los campos");
            return;
        }

        String sql = "INSERT INTO usuarios (nombre, correo, telefono, tipo_usuario) "
                + "VALUES (?, ?, ?, ?)";
        JsonObject resultado = new JsonObject();

        try (Connection con = abrirConexion();
             PreparedStatement consulta =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            consulta.setString(1, usuario.nombre.strip());
            consulta.setString(2, usuario.correo);
            consulta.setString(3, usuario.telefono);
            consulta.setString(4, usuario.tipoUsuario);
            consulta.executeUpdate();

            // Devuelve el ID para usarlo después al registrar un préstamo.
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (claves.next()) {
                    resultado.addProperty("idUsuario", claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            log("Error al registrar el usuario", e);
            responderError(response, 500, "No se pudo registrar el usuario");
            return;
        }

        resultado.addProperty("mensaje", "Usuario registrado correctamente");
        responder(response, 201, resultado);
    }

    private Connection abrirConexion() throws SQLException {
        Connection con = conexion.conectar();
        if (con == null) {
            throw new SQLException("No se pudo conectar a MariaDB");
        }
        return con;
    }

    private boolean excedeLongitud(String valor, int maximo) {
        return valor != null && valor.length() > maximo;
    }

    private void responderError(HttpServletResponse response, int estado, String mensaje)
            throws IOException {
        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty("error", mensaje);
        responder(response, estado, cuerpo);
    }

    private void responder(HttpServletResponse response, int estado,
            com.google.gson.JsonElement cuerpo) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        gson.toJson(cuerpo, response.getWriter());
    }

    private static class UsuarioDatos {
        private String nombre;
        private String correo;
        private String telefono;
        private String tipoUsuario;
    }
}