import java.io.*;
import javax.servlet.http.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import javax.servlet.ServletException;

public class Share extends HttpServlet
{
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException
  {
System.out.println("In share");
        Integer fileId = Integer.parseInt(request.getParameter("fileId"));
	Integer userId = Integer.parseInt(request.getParameter("targetUserId"));

System.out.println("fileId"+ fileId);
System.out.println("userId" + userId);
	 String FilePath = null;
       String FileName = null;
	Integer folderId = 0;
       String username = null;
       Long size = 0L;
	Boolean enc = false;
      
      
      try(Connection con = Database.getObject().getConnection())
	{
          String query1 = "SELECT username FROM users WHERE user_id= ?";
         try(PreparedStatement stmt1 = con.prepareStatement(query1))
	    {
		stmt1.setInt(1,userId);
		ResultSet rs1 = stmt1.executeQuery();
		if(rs1.next())
		{
 		    username = rs1.getString("username");
		} 
	    }

          String query2 = "SELECT folder_id FROM folders WHERE name = ?";
	  try(PreparedStatement stmt2 = con.prepareStatement(query2))
	      {
		stmt2.setString(1, username);
		ResultSet rs2 = stmt2.executeQuery();
		if(rs2.next())
		{
		    folderId = rs2.getInt("folder_id");
		}
 	       }

	   String query3 = "SELECT name,file_path,file_size,is_encrypted FROM files WHERE file_id= ?";
  
	   try(PreparedStatement stmt3 = con.prepareStatement(query3))
	   {
		stmt3.setInt(1,fileId);
		ResultSet rs3  = stmt3.executeQuery();	
		if(rs3.next())
		{
		   FileName = rs3.getString("name");
		   FilePath = rs3.getString("file_path");
		   size = rs3.getLong("file_size");
		   enc = rs3.getBoolean("is_encrypted");
		}
           }

           String query4 = "INSERT INTO files (name, folder_id, user_id,  file_path, file_size, is_encrypted) VALUES (?, ?, ?, ?, ?, ?)";
	 try(PreparedStatement stmt4 = con.prepareStatement(query4))
	   {
		stmt4.setString(1, FileName);
		stmt4.setInt(2, folderId);
		stmt4.setInt(3, userId);
		stmt4.setString(4, FilePath);
		stmt4.setLong(5, size);
		stmt4.setBoolean(6, enc);
		stmt4.executeUpdate();
	   }

	}catch(	SQLException e)
	{
	   e.printStackTrace();
	   response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"ERROR IN SHARE");
	return;
 	}

      response.setContentType("application/json");
      response.getWriter().write("{\"success\": true}");
  }
}