import java.io.IOException;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/deleteFolder")
public class DeleteFolderServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String folderIdStr = request.getParameter("folderId");

        if (folderIdStr == null || folderIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Folder ID parameter is missing.");
            return;
        }

        int folderId;
        try {
            folderId = Integer.parseInt(folderIdStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid folder ID format.");
            return;
        }
       

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in.");
            return;
        }

        try (Connection conn = Database.getObject().getConnection()) {
            
            List<String> filesToDelete = getAllFilesInFolder(folderId, userId, conn);

            
            for (String fileName : filesToDelete) {
                ArchiveManager.deleteFileFromArchive(fileName);
            }

            
            deleteFolderAndContents(folderId, userId, conn);

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", "Folder and its contents deleted successfully.");
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    private List<String> getAllFilesInFolder(int folderId, int userId, Connection conn) throws SQLException {
        List<String> filePaths = new ArrayList<>();
        getAllFilesInFolderRecursive(folderId, userId, conn, filePaths);
        return filePaths;
    }

    private void getAllFilesInFolderRecursive(int folderId, int userId, Connection conn, List<String> filePaths) throws SQLException {
        String query = "SELECT file_path FROM files WHERE folder_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, folderId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    filePaths.add(rs.getString("file_path"));
                }
            }
        }

        String subfoldersQuery = "SELECT folder_id FROM folders WHERE parent_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(subfoldersQuery)) {
            stmt.setInt(1, folderId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int subfolderId = rs.getInt("folder_id");
                    getAllFilesInFolderRecursive(subfolderId, userId, conn, filePaths);
                }
            }
        }
    }

    private void deleteFolderAndContents(int folderId, int userId, Connection conn) throws SQLException {
        
        String deleteFilesQuery = "DELETE FROM files WHERE folder_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteFilesQuery)) {
            stmt.setInt(1, folderId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }

       
        String selectSubfoldersQuery = "SELECT folder_id FROM folders WHERE parent_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSubfoldersQuery)) {
            stmt.setInt(1, folderId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int subfolderId = rs.getInt("folder_id");
                    deleteFolderAndContents(subfolderId, userId, conn);
                }
            }
        }

        
        String deleteFolderQuery = "DELETE FROM folders WHERE folder_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteFolderQuery)) {
            stmt.setInt(1, folderId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}
