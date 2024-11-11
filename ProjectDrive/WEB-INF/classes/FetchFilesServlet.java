
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/fetchFiles")
public class FetchFilesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        List<JSONObject> files = new ArrayList<>();

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        String folderIdStr = request.getParameter("folderId");
        Integer folderId;

        try {
            folderId = Integer.parseInt(folderIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid folderId format\"}");
            return;
        }

        if (folderId == 0 || folderId == null) {
            folderId = (Integer) session.getAttribute("rootFolderId");
        }

      

        try (Connection conn = Database.getObject().getConnection()) {
            String query = "SELECT * FROM files WHERE folder_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, folderId);
                stmt.setInt(2, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    JSONObject file = new JSONObject();
                    file.put("file_id", rs.getInt("file_id"));
                    file.put("name", rs.getString("name"));
                    file.put("folder_id", rs.getInt("folder_id"));
                    file.put("user_id", rs.getInt("user_id"));
                    file.put("file_path", rs.getString("file_path"));
                    files.add(file);
                }
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving files: " + e.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.getWriter().write(new JSONArray(files).toString());
    }
}
