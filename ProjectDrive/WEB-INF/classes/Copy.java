import java.io.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import javax.servlet.ServletException;

public class Copy extends HttpServlet
{
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
	Integer fileId = Integer.parseInt(request.getParameter("fileId"));
	Integer targetFolderId = Integer.parseInt(request.getParameter("targetFolderId"));
System.out.println("fileId " + fileId);
System.out.println(targetFolderId + " targetFolderId"); 
	String FileName = null;
	String FilePath = null;
	Long size = 0L;
	Boolean enc = false;

 	HttpSession session = request.getSession();
	Integer userId = (Integer)session.getAttribute("userId");
	

	
   	try(Connection con = Database.getObject().getConnection())
 	{
	   String query = "SELECT * FROM files WHERE file_id = ?";
  
	   try(PreparedStatement stmt = con.prepareStatement(query))
	   {
		stmt.setInt(1,fileId);
		ResultSet rs  = stmt.executeQuery();	
		if(rs.next())
		{
		   FileName = rs.getString("name");
		   FilePath = rs.getString("file_path");
		   size = rs.getLong("file_size");
		   enc = rs.getBoolean("is_encrypted");
		}
           }


	   String query1 = "INSERT INTO files (name, folder_id, user_id,  file_path, file_size, is_encrypted) VALUES (?, ?, ?, ?, ?, ?)"; 
  
	   try(PreparedStatement stmt1 = con.prepareStatement(query1))
	   {
		stmt1.setString(1, FileName);
		stmt1.setInt(2, targetFolderId);
		stmt1.setInt(3, userId);
		stmt1.setString(4, FilePath);
		stmt1.setLong(5, size);
		stmt1.setBoolean(6, enc);
		stmt1.executeUpdate();
		
           }
	}
	catch(SQLException e)
	{
		e.printStackTrace();
		
		return ;
	}
	response.setContentType("application/json");
	response.getWriter().write("{\"success\" : true}");
   }
}