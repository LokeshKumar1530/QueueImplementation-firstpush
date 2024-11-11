import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/MoveFolderServlet")
public class MoveFolderServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer folderId = Integer.parseInt(request.getParameter("itemId"));
        Integer targetId = Integer.parseInt(request.getParameter("targetFolderId"));
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        try (Connection con = Database.getObject().getConnection()) {
            String checkFolderQuery = "SELECT folder_id FROM folders WHERE folder_id = ? AND user_id = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkFolderQuery)) {
                checkStmt.setInt(1, targetId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String updateFolderQuery = "UPDATE folders SET parent_id = ? WHERE folder_id = ? AND user_id = ?";
                    try (PreparedStatement updateStmt = con.prepareStatement(updateFolderQuery)) {
                        updateStmt.setInt(1, targetId);
                        updateStmt.setInt(2, folderId);
                        updateStmt.setInt(3, userId);
                        int rowsAffected = updateStmt.executeUpdate();

                        if (rowsAffected > 0) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            try (PrintWriter out = response.getWriter()) {
                                out.println("Folder moved successfully");
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            try (PrintWriter out = response.getWriter()) {
                                out.println("Error: Unable to move the folder");
                            }
                        }
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Error: The entered Folder Id does not exist.");
                    }
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("Error: Database error occurred");
                e.printStackTrace(out);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("Error: An unexpected error occurred");
                e.printStackTrace(out);
            }
        }
    }
}














