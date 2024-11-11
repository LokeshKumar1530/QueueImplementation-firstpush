import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
	String password = request.getParameter("password");
	String email = request.getParameter("email");

	PrintWriter out = response.getWriter();
        boolean isCreated = false;

        try (Connection conn = Database.getObject().getConnection()) {
            conn.setAutoCommit(false); 

           
            String sqlUser = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement stmtUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, username);
            stmtUser.setString(2, password);
            stmtUser.setString(3, email);
            stmtUser.executeUpdate();

            ResultSet rsUser = stmtUser.getGeneratedKeys();
            int userId = -1;
            if (rsUser.next()) {
                userId = rsUser.getInt(1);
System.out.println("inside the signup resultset for users");
            }

            String sqlFolder = "INSERT INTO folders (name, parent_id, user_id) VALUES (?, ?, ?)";
            PreparedStatement stmtFolder = conn.prepareStatement(sqlFolder, PreparedStatement.RETURN_GENERATED_KEYS);
            stmtFolder.setString(1, username);
            stmtFolder.setNull(2, java.sql.Types.INTEGER); 
            stmtFolder.setInt(3, userId);
            stmtFolder.executeUpdate();

            ResultSet rsFolder = stmtFolder.getGeneratedKeys();
            int rootFolderId = -1;
            if (rsFolder.next()) {
                rootFolderId = rsFolder.getInt(1);
System.out.println("Inside the signup resultset for folders");
            }

            conn.commit();
            isCreated = true;
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        out.println("{ \"success\": " + isCreated + " }");
        out.flush();
    }
}
    