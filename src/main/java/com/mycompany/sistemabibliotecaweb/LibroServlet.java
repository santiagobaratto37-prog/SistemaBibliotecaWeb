package com.mycompany.sistemabibliotecaweb;

import java.io.IOException;
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

        request.setAttribute("libros", dao.listar());
        request.getRequestDispatcher("/WEB-INF/libros.jsp")
                .forward(request, response);
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
            response.sendError(400, "Verifica los datos numéricos.");
            return;
        }

        response.sendRedirect(
                request.getContextPath() + "/LibroServlet?resultado=" + resultado
        );
    }
}