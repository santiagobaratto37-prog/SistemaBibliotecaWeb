package com.mycompany.sistemabibliotecaweb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/categorias")
public class CategoriasApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String sql = "SELECT id_categoria, nombre, literatura "
                + "FROM categorias ORDER BY id_categoria";
        JsonArray categorias = new JsonArray();

        try (Connection con = abrirConexion()) {
            if (con == null) {
                throw new SQLException("No se pudo conectar a MariaDB");
            }

            try (PreparedStatement consulta = con.prepareStatement(sql);
                 ResultSet filas = consulta.executeQuery()) {

                // Usa los nombres de la API para representar cada categoría.
                while (filas.next()) {
                    JsonObject categoria = new JsonObject();
                    categoria.addProperty("idCategoria", filas.getInt("id_categoria"));
                    categoria.addProperty("nombre", filas.getString("nombre"));
                    categoria.addProperty("literatura", filas.getInt("literatura"));
                    categorias.add(categoria);
                }
            }
        } catch (SQLException e) {
            log("Error al consultar las categorías", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"No se pudieron consultar las categorías\"}");
            return;
        }

        gson.toJson(categorias, response.getWriter());
    }
    // Permite simular JDBC en las pruebas sin modificar la base de datos.
    protected Connection abrirConexion() {
        return conexion.conectar();
    }
}