
package com.mycompany.sistemabibliotecaweb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/biblioteca";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection conectar() {

        try {
            Class.forName("org.mariadb.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    URL, USER, PASSWORD);

            System.out.println("CONEXION EXITOSA CON LA BASE DE DATOS");

            return con;

        } catch (ClassNotFoundException | SQLException e) {

            System.out.println("ERROR DE CONEXION:");
            e.printStackTrace();

            return null;
        }
    }
}
