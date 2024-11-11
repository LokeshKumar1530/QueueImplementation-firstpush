import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
      
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        boolean isAuthenticated = false;
        int userId = -1;
        int rootFolderId = -1;

        try (Connection conn = Database.getObject().getConnection()) {
            String sql = "SELECT u.user_id, f.folder_id " +
                         "FROM users u JOIN folders f ON u.user_id = f.user_id " +
                         "WHERE u.username = ? AND u.password = ? AND f.name = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, username); 
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("user_id");
                rootFolderId = rs.getInt("folder_id");
                isAuthenticated = true;
            } else {
                out.println("{ \"success\": false, \"message\": \"Invalid username or password\" }");
                out.flush();
		response.sendRedirect("login.html?error=invalide");
                return; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (isAuthenticated) {
            HttpSession session = request.getSession();
            session.setAttribute("userId", userId);
            session.setAttribute("rootFolderId", rootFolderId);
            session.setAttribute("username", username);
            out.println("{ \"success\": true }");
        } else {
            out.println("{ \"success\": false, \"message\": \"Invalid username or password\" }");
        }

  
    }
}
