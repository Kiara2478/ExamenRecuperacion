/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import com.google.gson.Gson;
import dto.Medico;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author kiara
 */
@WebServlet(name = "MedicoCRUD", urlPatterns = {"/medicocrud"})
public class MedicoCRUD extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        try {
            if ("list".equals(action)) {
                listStudents(out);
            } else if ("get".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr != null) {
                    getStudent(Long.parseLong(idStr), out);
                } else {
                    sendErrorResponse(out, "ID requerido");
                }
            } else {
                sendErrorResponse(out, "Acción no válida");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error interno del servidor");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                addStudent(request, out);
            } else if ("update".equals(action)) {
                updateStudent(request, out);
            } else if ("delete".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr != null) {
                    deleteStudent(Long.parseLong(idStr), out);
                } else {
                    sendErrorResponse(out, "ID requerido");
                }
            } else if ("changePassword".equals(action)) {
                changePassword(request, out);
            } else {
                sendErrorResponse(out, "Acción no válida");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error interno del servidor");
        }
    }

    private void listStudents(PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT m FROM Medico m ORDER BY m.codiMedi");
            List<Medico> medicos = query.getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("student", medicos);

            Gson gson = new Gson();
            out.print(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error al obtener la lista de medicos");
        } finally {
            em.close();
        }
    }

    private void getStudent(Long id, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            Medico student = em.find(Medico.class, id);

            if (student != null) {
                // Crear un objeto con los datos del estudiante para edición
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("id", student.getCodiMedi());
                studentData.put("dni", student.getNdniMedi());
                studentData.put("login", student.getLogiMedi());
                studentData.put("apPaterno", student.getAppaMedi());
                studentData.put("apMaterno", student.getApmaMedi());
                studentData.put("nombre", student.getNombMedi());

                // Formatear fecha para el input date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                studentData.put("fechaNacimiento", dateFormat.format(student.getFechNaciMedi()));

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("student", studentData);

                Gson gson = new Gson();
                out.print(gson.toJson(response));
            } else {
                sendErrorResponse(out, "Medico no encontrado");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error al obtener al medico");
        } finally {
            em.close();
        }
    }

    private void addStudent(HttpServletRequest request, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            // Validar que no exista el DNI
            String dni = request.getParameter("dni");
            String login = request.getParameter("login");

            Query checkDni = em.createQuery("SELECT COUNT(m) FROM Medico m WHERE m.ndniMedi = :dni");
            checkDni.setParameter("dni", dni);
            Long dniCount = (Long) checkDni.getSingleResult();

            if (dniCount > 0) {
                sendErrorResponse(out, "Ya existe un medico con ese DNI");
                return;
            }

            Query checkLogin = em.createQuery("SELECT COUNT(m) FROM Medico m WHERE m.logiMedi = :login");
            checkLogin.setParameter("login", login);
            Long loginCount = (Long) checkLogin.getSingleResult();

            if (loginCount > 0) {
                sendErrorResponse(out, "Ya existe un medico con ese login");
                return;
            }

            em.getTransaction().begin();

            Medico estudiante = new Medico();
            estudiante.setNdniMedi(dni);
            estudiante.setLogiMedi(login);
            estudiante.setAppaMedi(request.getParameter("apPaterno"));
            estudiante.setApmaMedi(request.getParameter("apMaterno"));
            estudiante.setNombMedi(request.getParameter("nombre"));
            estudiante.setPassMedi(request.getParameter("password"));

            // Convertir fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNacimiento = dateFormat.parse(request.getParameter("fechaNacimiento"));
            estudiante.setFechNaciMedi(fechaNacimiento);

            em.persist(estudiante);
            em.getTransaction().commit();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Medico agregado exitosamente");

            Gson gson = new Gson();
            out.print(gson.toJson(response));

        } catch (ParseException e) {
            em.getTransaction().rollback();
            sendErrorResponse(out, "Formato de fecha inválido");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            sendErrorResponse(out, "Error al agregar al medico");
        } finally {
            em.close();
        }
    }

    private void updateStudent(HttpServletRequest request, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            String dni = request.getParameter("dni");
            String login = request.getParameter("login");

            // Validar que no exista otro estudiante con el mismo DNI o login
            Query checkDni = em.createQuery("SELECT COUNT(m) FROM Medico m WHERE m.ndniMedi = :dni AND m.codiMedi != :id");
            checkDni.setParameter("dni", dni);
            checkDni.setParameter("id", id);
            Long dniCount = (Long) checkDni.getSingleResult();

            if (dniCount > 0) {
                sendErrorResponse(out, "Ya existe otro medico con ese DNI");
                return;
            }

            Query checkLogin = em.createQuery("SELECT COUNT(m) FROM Medico m WHERE m.logiMedi = :login AND m.codiMedi != :id");
            checkLogin.setParameter("login", login);
            checkLogin.setParameter("id", id);
            Long loginCount = (Long) checkLogin.getSingleResult();

            if (loginCount > 0) {
                sendErrorResponse(out, "Ya existe otro medico con ese login");
                return;
            }

            em.getTransaction().begin();

            Medico estudiante = em.find(Medico.class, id);
            if (estudiante != null) {
                estudiante.setNdniMedi(dni);
                estudiante.setLogiMedi(login);
                estudiante.setAppaMedi(request.getParameter("apPaterno"));
                estudiante.setApmaMedi(request.getParameter("apMaterno"));
                estudiante.setNombMedi(request.getParameter("nombre"));

                // Convertir fecha
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date fechaNacimiento = dateFormat.parse(request.getParameter("fechaNacimiento"));
                estudiante.setFechNaciMedi(fechaNacimiento);

                em.merge(estudiante);
                em.getTransaction().commit();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Medico actualizado exitosamente");

                Gson gson = new Gson();
                out.print(gson.toJson(response));
            } else {
                em.getTransaction().rollback();
                sendErrorResponse(out, "Medico no encontrado");
            }

        } catch (ParseException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            sendErrorResponse(out, "Formato de fecha inválido");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            sendErrorResponse(out, "Error al actualizar al medico");
        } finally {
            em.close();
        }
    }

    private void deleteStudent(Long id, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Medico estudiante = em.find(Medico.class, id);
            if (estudiante != null) {
                em.remove(estudiante);
                em.getTransaction().commit();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Medico eliminado exitosamente");

                Gson gson = new Gson();
                out.print(gson.toJson(response));
            } else {
                em.getTransaction().rollback();
                sendErrorResponse(out, "Medico no encontrado");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            sendErrorResponse(out, "Error al eliminar al medico");
        } finally {
            em.close();
        }
    }

    private void changePassword(HttpServletRequest request, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        try {
            HttpSession session = request.getSession();
            Long userId = (Long) session.getAttribute("userId"); // Asumiendo que el ID del usuario está en la sesión

            if (userId == null) {
                sendErrorResponse(out, "Usuario no autenticado");
                return;
            }

            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");

            em.getTransaction().begin();

            Medico estudiante = em.find(Medico.class, userId);
            if (estudiante != null) {
                // Verificar contraseña actual
                if (!estudiante.getPassMedi().equals(currentPassword)) {
                    em.getTransaction().rollback();
                    sendErrorResponse(out, "La contraseña actual es incorrecta");
                    return;
                }

                // Actualizar contraseña
                estudiante.setPassMedi(newPassword);
                em.merge(estudiante);
                em.getTransaction().commit();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Contraseña cambiada exitosamente");

                Gson gson = new Gson();
                out.print(gson.toJson(response));
            } else {
                em.getTransaction().rollback();
                sendErrorResponse(out, "Usuario no encontrado");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            sendErrorResponse(out, "Error al cambiar la contraseña");
        } finally {
            em.close();
        }
    }

    private void sendErrorResponse(PrintWriter out, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);

        Gson gson = new Gson();
        out.print(gson.toJson(response));
    }
}
