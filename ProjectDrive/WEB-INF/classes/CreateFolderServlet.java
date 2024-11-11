import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;

import org.json.JSONObject;

@WebServlet("/createFolder")
public class CreateFolderServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String parentIdStr = request.getParameter("parentId");

        HttpSession session = request.getSession();
        Integer parentId = parentIdStr != null ? Integer.parseInt(parentIdStr) : (Integer) session.getAttribute("rootFolderId");
        Integer userId = (Integer) session.getAttribute("userId");

        if (parentId == 0) {
            parentId = (Integer) session.getAttribute("rootFolderId");
        }

        if (parentId == null || userId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid session attributes.");
            return;
        }

        try (Connection conn = Database.getObject().getConnection()) {
           
            String checkDuplicateQuery = "SELECT COUNT(*) FROM folders WHERE name = ? AND parent_id = ? AND user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateQuery)) {
                checkStmt.setString(1, name);
                checkStmt.setInt(2, parentId);
                checkStmt.setInt(3, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    PrintWriter out = response.getWriter();
                    out.print("{\"message\": \"Folder with the same name already exists.\"}");
                    out.flush();
                    return;
                }
            }

           
            String query = "INSERT INTO folders (name, parent_id, user_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setInt(2, parentId);
                stmt.setInt(3, userId);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    throw new SQLException("Creating folder failed, no rows affected.");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parent ID format.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
