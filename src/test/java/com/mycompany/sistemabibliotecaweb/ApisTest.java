package com.mycompany.sistemabibliotecaweb;
import com.google.gson.JsonParser;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** JDBC simulado: estas pruebas no acceden a la base real. */
public class ApisTest {
 private HttpServletRequest req; private HttpServletResponse res;
 private Connection con; private PreparedStatement sql; private ResultSet filas;
 private StringWriter salida;
 @Before public void preparar() throws Exception {
  req=mock(HttpServletRequest.class); res=mock(HttpServletResponse.class);
  con=mock(Connection.class); sql=mock(PreparedStatement.class); filas=mock(ResultSet.class);
  salida=new StringWriter(); when(res.getWriter()).thenReturn(new PrintWriter(salida));
  when(con.prepareStatement(anyString())).thenReturn(sql);
  when(con.prepareStatement(anyString(),anyInt())).thenReturn(sql);
  when(sql.executeQuery()).thenReturn(filas); when(sql.getGeneratedKeys()).thenReturn(filas);
 }
 private void cuerpo(String s) throws Exception { when(req.getReader()).thenReturn(new BufferedReader(new StringReader(s))); }
 private UsuariosApiServlet usuarios(){ return new UsuariosApiServlet(){ @Override protected Connection abrirConexion(){return con;} }; }
 private PrestamosApiServlet prestamos(){ return new PrestamosApiServlet(){ @Override protected Connection abrirConexion(){return con;} }; }
 @Test public void categoriasSerializaDatos() throws Exception {
  when(filas.next()).thenReturn(true,false); when(filas.getInt("id_categoria")).thenReturn(3);
  when(filas.getString("nombre")).thenReturn("Ciencia");
  new CategoriasApiServlet(){ @Override protected Connection abrirConexion(){return con;} }.doGet(req,res);
  com.google.gson.JsonObject dato=JsonParser.parseString(salida.toString()).getAsJsonArray().get(0).getAsJsonObject();
  assertEquals(3,dato.get("idCategoria").getAsInt()); assertEquals("Ciencia",dato.get("nombre").getAsString()); verify(con).close();
 }
 @Test public void usuariosRechazaNombreVacio() throws Exception {
  cuerpo("{\"nombre\":\"   \"}"); usuarios().doPost(req,res);
  verify(res).setStatus(400); assertTrue(JsonParser.parseString(salida.toString()).getAsJsonObject().has("error")); verifyNoInteractions(con);
 }
 @Test public void usuariosRechazaNombreLargo() throws Exception {
  cuerpo("{\"nombre\":\""+"a".repeat(151)+"\"}"); usuarios().doPost(req,res);
  verify(res).setStatus(400); verifyNoInteractions(con);
 }
 @Test public void usuariosRegistraYDevuelveId() throws Exception {
  cuerpo("{\"nombre\":\" Ana \"}"); when(filas.next()).thenReturn(true); when(filas.getInt(1)).thenReturn(8);
  usuarios().doPost(req,res); verify(sql).setString(1,"Ana"); verify(sql).executeUpdate(); verify(res).setStatus(201);
  assertEquals(8,JsonParser.parseString(salida.toString()).getAsJsonObject().get("idUsuario").getAsInt());
 }
 @Test public void prestamosRechazaIdsInvalidos() throws Exception {
  cuerpo("{\"idUsuario\":0,\"idLibro\":-1}"); prestamos().doPost(req,res); verify(res).setStatus(400); verifyNoInteractions(con);
 }
 @Test public void prestamosSinStockRevierteTransaccion() throws Exception {
  cuerpo("{\"idUsuario\":1,\"idLibro\":2}"); when(filas.next()).thenReturn(true); when(sql.executeUpdate()).thenReturn(0);
  prestamos().doPost(req,res); verify(con).rollback(); verify(con,never()).commit(); verify(res).setStatus(409); assertTrue(salida.toString().contains("stock"));
 }
 @Test public void prestamosConfirmaRegistro() throws Exception {
  cuerpo("{\"idUsuario\":1,\"idLibro\":2}"); when(filas.next()).thenReturn(true); when(sql.executeUpdate()).thenReturn(1); when(filas.getInt(1)).thenReturn(9);
  prestamos().doPost(req,res); verify(con).commit(); verify(con,never()).rollback(); verify(res).setStatus(201);
  assertEquals(9,JsonParser.parseString(salida.toString()).getAsJsonObject().get("idPrestamo").getAsInt());
 }
 @Test public void devolucionDuplicadaNoIncrementaStock() throws Exception {
  when(req.getPathInfo()).thenReturn("/9/devolucion"); when(filas.next()).thenReturn(true);
  when(filas.getDate("fecha_devolucion")).thenReturn(Date.valueOf("2026-11-11"));
  prestamos().doPut(req,res); verify(res).setStatus(409); verify(con).rollback(); verify(sql,never()).executeUpdate(); verify(con,never()).commit();
 }
}
