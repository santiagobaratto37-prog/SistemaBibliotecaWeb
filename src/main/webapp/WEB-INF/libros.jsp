<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.mycompany.sistemabibliotecaweb.libro" %>

<%!
    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    List<?> libros = (List<?>) request.getAttribute("libros");
    String ruta = request.getContextPath() + "/LibroServlet";
    String resultado = request.getParameter("resultado");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Sistema Biblioteca</title>
</head>
<body>
    <h1>Gestión de libros</h1>

    <% if ("true".equals(resultado)) { %>
        <p>Operación realizada correctamente.</p>
    <% } else if ("false".equals(resultado)) { %>
        <p>No se pudo completar la operación.</p>
    <% } %>

    <h2>Registrar libro</h2>
    <form method="post" action="<%= escaparHtml(ruta) %>">
        <input type="hidden" name="accion" value="insertar">

        <label>Título <input name="titulo" maxlength="150" required></label>
        <label>Autor <input name="autor" maxlength="100"></label>
        <label>Stock <input type="number" name="stock" min="0" required></label>
        <label>Categoría <input type="number" name="idCategoria" min="1" required></label>
        <button type="submit">Registrar</button>
    </form>

    <h2>Libros registrados</h2>
    <% if (libros.isEmpty()) { %>
        <p>No hay libros registrados.</p>
    <% } else { %>
        <table border="1" cellpadding="8">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Título</th>
                    <th>Autor</th>
                    <th>Stock</th>
                    <th>Categoría</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <% for (Object elemento : libros) {
                    libro item = (libro) elemento;
                %>
                    <tr>
                        <td><%= item.getIdLibro() %></td>
                        <td><%= escaparHtml(item.getTitulo()) %></td>
                        <td><%= escaparHtml(item.getAutor()) %></td>
                        <td><%= item.getStock() %></td>
                        <td><%= item.getIdCategoria() %></td>
                        <td>
                            <form method="post" action="<%= escaparHtml(ruta) %>">
                                <input type="hidden" name="accion" value="actualizar">
                                <input type="hidden" name="id" value="<%= item.getIdLibro() %>">

                                <label>Título
                                    <input name="titulo" maxlength="150" required
                                           value="<%= escaparHtml(item.getTitulo()) %>">
                                </label>
                                <label>Autor
                                    <input name="autor" maxlength="100"
                                           value="<%= escaparHtml(item.getAutor()) %>">
                                </label>
                                <label>Stock
                                    <input type="number" name="stock" min="0" required
                                           value="<%= item.getStock() %>">
                                </label>
                                <label>Categoría
                                    <input type="number" name="idCategoria" min="1" required
                                           value="<%= item.getIdCategoria() %>">
                                </label>
                                <button type="submit">Actualizar</button>
                            </form>

                            <form method="post" action="<%= escaparHtml(ruta) %>">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="<%= item.getIdLibro() %>">
                                <button type="submit">Eliminar</button>
                            </form>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    <% } %>
</body>
</html>