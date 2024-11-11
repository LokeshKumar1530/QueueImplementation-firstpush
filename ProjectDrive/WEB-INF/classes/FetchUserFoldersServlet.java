import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
public class FetchUserFoldersServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("userId");
        List<JSONObject> folders = new ArrayList<>();
        
        try (Connection conn = Database.getObject().getConnection()) {
            String query = "SELECT * FROM folders WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject folder = new JSONObject();
                    folder.put("folder_id", rs.getInt("folder_id"));
                    folder.put("name", rs.getString("name"));
                    folder.put("parent_id", rs.getInt("parent_id"));
                    folder.put("user_id", rs.getInt("user_id"));
                    folders.add(folder);
                }
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving folders: " + e.getMessage());
            return;
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("folders", folders);
        
        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }
}
*/


public class FetchUserFoldersServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("userId");

        JSONArray foldersArray = new JSONArray();

        try (Connection conn = Database.getObject().getConnection()) {
            String query = "SELECT * FROM folders WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject folderJson = new JSONObject();
                    folderJson.put("folder_id", rs.getInt("folder_id"));
                    folderJson.put("name", rs.getString("name"));
                    folderJson.put("parent_id", rs.getInt("parent_id"));
                    folderJson.put("user_id", rs.getInt("user_id"));
                    foldersArray.put(folderJson);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving folders: " + e.getMessage());
            return;
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("folders", foldersArray);

        response.setContentType("application/json");
        response.getWriter().write(responseJson.toString());
    }
}
