package com.mycompany.sistemabibliotecaweb;

public class libro {

    private int idLibro;
    private String titulo;
    private String autor;
    private int stock;
    private int idCategoria;

    public libro() {
    }

    public libro(int idLibro, String titulo, String autor,
                 int stock, int idCategoria) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
}