import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchUsers extends HttpServlet
{
  protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException,IOException
  {
	HttpSession session = request.getSession();
	int userId = (Integer)session.getAttribute("userId");
	List<JSONObject> users = new ArrayList<>();
	
	try(Connection con = Database.getObject().getConnection())
	{
	 String query = "SELECT user_id,username FROM users WHERE user_id != ?";
	 try(PreparedStatement stmt = con.prepareStatement(query))
	 {
		stmt.setInt(1, userId);
		ResultSet rs = stmt.executeQuery();
		while(rs.next())
		{
		JSONObject user = new JSONObject();
		user.put("user_id", rs.getInt("user_id"));
		user.put("username", rs.getString("username"));
		users.add(user);
		}
	 }
	}catch(	SQLException | JSONException e)
	{
	 e.printStackTrace();
	 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR IN FETCHUSERS"+ e.getMessage());
    	return;
	}

	JSONObject responsej = new JSONObject();
	responsej.put("users", users);
	response.setContentType("application/json");
	response.getWriter().write(responsej.toString());
  }
}