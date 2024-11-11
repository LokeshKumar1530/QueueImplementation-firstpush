import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/getParentFolderId")
public class ParentFolderServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int folderId = Integer.parseInt(request.getParameter("folderId"));
  
  	HttpSession session = request.getSession();
        Integer userId = (Integer)session.getAttribute("userId");

        int parentId = getParentFolderId(folderId, userId);

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.valueOf(parentId));
    }

    private int getParentFolderId(int folderId, int userId) throws ServletException {
        int parentId = 0;
 	
        try (Connection conn = Database.getObject().getConnection()) {
            String query = "SELECT parent_id FROM folders WHERE folder_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, folderId);
		stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    parentId = rs.getInt("parent_id");
                } else {
                    throw new ServletException("Folder with ID " + folderId + " not found.");
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error: " + e.getMessage());
        }

        return parentId;
    }
}
