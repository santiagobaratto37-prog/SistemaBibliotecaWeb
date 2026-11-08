
package com.mycompany.sistemabibliotecaweb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LibroServlet", urlPatterns = {"/LibroServlet"})
public class LibroServlet extends HttpServlet {

    private final librosDAO dao = new librosDAO();
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        List<libro> libros = dao.listar();

        try (PrintWriter out = response.getWriter()) {

            out.println("<html><head><title>Sistema Biblioteca</title></head><body>");
            out.println("<h1>Gestion de libros</h1>");

            out.println("<h2>Registrar libro</h2>");
            out.println("<form method='post' action='LibroServlet'>");
            out.println("<input type='hidden' name='accion' value='insertar'>");

            out.println("Titulo: <input name='titulo' maxlength='150' required><br><br>");
            out.println("Autor: <input name='autor' maxlength='100'><br><br>");
            out.println("Stock: <input type='number' name='stock' min='0' required><br><br>");
            out.println("ID Categoria: <input type='number' name='idCategoria' min='1' required><br><br>");

            out.println("<button type='submit'>Registrar</button>");
            out.println("</form>");

            out.println("<h2>Libros registrados</h2>");
            out.println("<table border='1' cellpadding='8'>");
            out.println("<tr><th>ID</th><th>Titulo</th><th>Autor</th><th>Stock</th><th>Categoria</th><th>Acciones</th></tr>");

            for (libro libro : libros) {

                out.println("<tr>");
                out.println("<td>" + libro.getIdLibro() + "</td>");
                out.println("<td>" + libro.getTitulo() + "</td>");
                out.println("<td>" + libro.getAutor() + "</td>");
                out.println("<td>" + libro.getStock() + "</td>");
                out.println("<td>" + libro.getIdCategoria() + "</td>");

                out.println("<td>");

                out.println("<form method='post' action='LibroServlet'>");
                out.println("<input type='hidden' name='accion' value='eliminar'>");
                out.println("<input type='hidden' name='id' value='" + libro.getIdLibro() + "'>");
                out.println("<button type='submit'>Eliminar</button>");
                out.println("</form>");

                out.println("<form method='post' action='LibroServlet'>");
                out.println("<input type='hidden' name='accion' value='actualizar'>");
                out.println("<input type='hidden' name='id' value='" + libro.getIdLibro() + "'>");
                out.println("<input name='titulo' value='" + libro.getTitulo() + "' required>");
                out.println("<input name='autor' value='" + libro.getAutor() + "'>");
                out.println("<input type='number' name='stock' value='" + libro.getStock() + "' min='0' required>");
                out.println("<input type='number' name='idCategoria' value='" + libro.getIdCategoria() + "' min='1' required>");
                out.println("<button type='submit'>Actualizar</button>");
                out.println("</form>");

                out.println("</td></tr>");
            }

            out.println("</table>");
            out.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        boolean resultado = false;

        try {

            if ("insertar".equals(accion)) {

                libro libro = new libro();
                libro.setTitulo(request.getParameter("titulo"));
                libro.setAutor(request.getParameter("autor"));
                libro.setStock(Integer.parseInt(request.getParameter("stock")));
                libro.setIdCategoria(Integer.parseInt(request.getParameter("idCategoria")));

                resultado = dao.insertar(libro);

            } else if ("actualizar".equals(accion)) {

                libro libro = new libro();
                libro.setIdLibro(Integer.parseInt(request.getParameter("id")));
                libro.setTitulo(request.getParameter("titulo"));
                libro.setAutor(request.getParameter("autor"));
                libro.setStock(Integer.parseInt(request.getParameter("stock")));
                libro.setIdCategoria(Integer.parseInt(request.getParameter("idCategoria")));

                resultado = dao.actualizar(libro);

            } else if ("eliminar".equals(accion)) {

                int id = Integer.parseInt(request.getParameter("id"));
                resultado = dao.eliminar(id);
            }

        } catch (NumberFormatException e) {

            response.sendError(400, "Verifica los datos numericos.");
            return;
        }

        response.sendRedirect(
                request.getContextPath() + "/LibroServlet?resultado=" + resultado
        );
    }
}
