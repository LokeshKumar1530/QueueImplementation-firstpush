import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



public class DeleteFileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        String fileIdStr = request.getParameter("fileId");
        int fileId = fileIdStr != null ? Integer.parseInt(fileIdStr) : 0;

        try (Connection conn = Database.getObject().getConnection()) {
            String fileName = getFileName(fileId, userId, conn);
            if (fileName != null) {
               
                ArchiveManager.deleteFileFromArchive(fileName);

               
                deleteFileFromDatabase(fileId, userId, conn);

                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"success\": false, \"message\": \"File not found.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getFileName(int fileId, int userId, Connection conn) throws SQLException {
        String query = "SELECT file_path FROM files WHERE file_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, fileId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file_path");
                } else {
                    return null;
                }
            }
        }
    }

    private void deleteFileFromDatabase(int fileId, int userId, Connection conn) throws SQLException {
        String query = "DELETE FROM files WHERE file_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, fileId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to delete file. File not found or unauthorized.");
            }
        }
    }
}
