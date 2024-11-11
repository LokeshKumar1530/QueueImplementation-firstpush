import javax.servlet.ServletException;
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

public class MoveFileServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
System.out.println("IN the move file servlet");
        Integer fileId = Integer.parseInt(request.getParameter("itemId"));
        Integer folderId = Integer.parseInt(request.getParameter("targetFolderId"));

   System.out.println(fileId  + " : fileId");
   System.out.println(folderId + " : folderId");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        try (Connection conn = Database.getObject().getConnection()) {
           
            String checkFolderQuery = "SELECT folder_id FROM folders WHERE folder_id = ? AND user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkFolderQuery)) {
System.out.println("In the check");
                checkStmt.setInt(1, folderId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                   
                    String updateFileQuery = "UPDATE files SET folder_id = ? WHERE file_id = ? AND user_id = ?";
System.out.println("In the update");
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateFileQuery)) {
                        updateStmt.setInt(1, folderId);
                        updateStmt.setInt(2, fileId);
                        updateStmt.setInt(3, userId);
                        int rowsAffected = updateStmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            try (PrintWriter out = response.getWriter()) {
                                out.println("File moved successfully.");
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            try (PrintWriter out = response.getWriter()) {
                                out.println("Error: Unable to move the file.");
                            }
                        }
                    }
                } else {
                    
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.println("Error: The entered folder ID does not exist or does not Present.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("Error: An internal server error occurred.");
            }
        }
    }
}
