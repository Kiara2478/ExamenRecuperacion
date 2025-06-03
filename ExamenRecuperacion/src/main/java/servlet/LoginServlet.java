/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author kiara
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/loginservlet"})
public class LoginServlet extends HttpServlet {
    
    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        // Inicialización única del EntityManagerFactory
        emf = Persistence.createEntityManagerFactory("com.mycompany_ExamenRecuperacion_war_1.0-SNAPSHOTPU");
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Obtener parámetros del formulario
        String ndni = request.getParameter("ndni");
        String password = request.getParameter("password");
        
        // Validar que los campos no estén vacíos
        if (ndni == null || ndni.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            response.sendRedirect("login.html?error=2");
            return;
        }
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            // Buscar usuario por DNI usando el campo correcto
            String jpql = "SELECT m FROM Medico m WHERE m.ndniMedi = :ndni";
            Object usuario = em.createQuery(jpql)
                              .setParameter("ndni", ndni.trim())
                              .getSingleResult();
            
            // Obtener el hash de la contraseña usando reflection o método getter
            String hashedPassword = getPasswordFromEntity(usuario);
            
            if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
                // Login exitoso
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                session.setAttribute("ndni", ndni);
                
                // Redirigir al dashboard o página principal
                response.sendRedirect("tables.html");
            } else {
                // Contraseña incorrecta
                response.sendRedirect("login.html?error=1");
            }
            
        } catch (NoResultException e) {
            // Usuario no encontrado
            response.sendRedirect("login.html?error=1");
        } catch (Exception e) {
            // Error del sistema
            e.printStackTrace();
            response.sendRedirect("login.html?error=3");
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    private String getPasswordFromEntity(Object entity) {
        try {
            // Opción 1: Si tienes un método getter
            java.lang.reflect.Method method = entity.getClass().getMethod("getPassMedi");
            return (String) method.invoke(entity);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
