
package com.mycompany.sistemabibliotecaweb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class librosDAO {

    // LISTAR LIBROS
    public List<libro> listar() {
        List<libro> lista = new ArrayList<>();

        String sql = "SELECT * FROM libros";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                libro l = new libro();

                l.setIdLibro(rs.getInt("id_libro"));
                l.setTitulo(rs.getString("titulo"));
                l.setAutor(rs.getString("autor"));
                l.setStock(rs.getInt("stock"));
                l.setIdCategoria(rs.getInt("id_categoria"));

                lista.add(l);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // INSERTAR LIBRO
    public boolean insertar(libro l) {

        String sql = "INSERT INTO libros "
                + "(titulo, autor, stock, id_categoria) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setInt(3, l.getStock());
            ps.setInt(4, l.getIdCategoria());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ACTUALIZAR LIBRO
    public boolean actualizar(libro l) {

        String sql = "UPDATE libros SET "
                + "titulo=?, autor=?, stock=?, id_categoria=? "
                + "WHERE id_libro=?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setInt(3, l.getStock());
            ps.setInt(4, l.getIdCategoria());
            ps.setInt(5, l.getIdLibro());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ELIMINAR LIBRO
    public boolean eliminar(int id) {

        String sql = "DELETE FROM libros WHERE id_libro=?";

        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
