import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
//SingleTon connection
public class Database
{
   private static Database object;
   private Connection connection;
   private String url = "jdbc:mysql://localhost:3306/finalProject";
   // Write your username and password
   private String username = "***";
   private String password = "********";

    private Database() throws SQLException
    {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        }catch(ClassNotFoundException e)
        {
            throw new SQLException(e);
        }
    }

   public Connection getConnection()
    { 
         return connection;
    }

   public static Database getObject() throws SQLException 
    {
        if(object == null)
           object = new Database();
        else if(object.getConnection().isClosed())
           object = new Database();

      return object;
    }
}