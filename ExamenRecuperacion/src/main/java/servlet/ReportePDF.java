/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author kiara
 */
@WebServlet(name = "ReportePDF", urlPatterns = {"/reportePDF"})
public class ReportePDF extends HttpServlet {
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    Connection conn = null;
    try {
        

        // 1. Conectar a la base de datos
       Class.forName("com.mysql.cj.jdbc.Driver");
       conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Matriculados", "root", ""); // Ajusta tu user y pass

       
       
        // 2. Obtener la ruta del .jasper
        String rutaJasper = getServletContext().getRealPath("/WEB-INF/reportes/reporte.jasper");

        // 3. Parámetros opcionales (puedes poner null si no usas)
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("titulo", "Reporte generado desde Java Web");

        // 4. Generar el reporte
        JasperPrint print = JasperFillManager.fillReport(rutaJasper, parametros, conn);

        // 5. Enviar el PDF como respuesta
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=reporte.pdf");

        OutputStream out = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(print, out);
        out.flush();
        out.close();

    } catch (Exception e) {
        e.printStackTrace();
        response.setContentType("text/plain");
       
        
        // Imprimir causa exacta
    Throwable cause = e.getCause();
    if (cause != null) {
        response.getWriter().println("Error generando el reporte: " + cause.getMessage());
    } else {
        response.getWriter().println("Error generando el reporte: " + e.getMessage());
    }
    } finally {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
    }
}
}


   