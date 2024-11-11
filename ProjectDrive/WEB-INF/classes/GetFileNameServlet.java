import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/getFileNameServlet")
public class GetFileNameServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int fileId = Integer.parseInt(request.getParameter("fileId"));

        try (Connection conn = Database.getObject().getConnection()) {
            String fileName = getFileName(fileId, conn);
            if (fileName != null) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("fileName", fileName);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getFileName(int fileId, Connection conn) throws SQLException {
        String query = "SELECT name FROM files WHERE file_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            } else {
                throw new SQLException("File not found for ID: " + fileId);
            }
        }
    }
}
