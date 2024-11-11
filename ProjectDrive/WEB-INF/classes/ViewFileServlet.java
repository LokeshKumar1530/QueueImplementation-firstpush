import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;

@WebServlet("/viewFileServlet")
public class ViewFileServlet extends HttpServlet {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "mySuperSecretKey"; 
    private static final String ARCHIVE_PATH = "C:\\Users\\LOKESH KUMAR\\OneDrive\\Desktop\\Dummy\\customArchive.bin";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession();
	Integer userId  = (Integer)session.getAttribute("userId");
 	if(userId == null)
	{
	   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	   return;
	}

        int fileId = Integer.parseInt(request.getParameter("fileId"));

        try (Connection conn = Database.getObject().getConnection()) {
            String fileName = getFileName(fileId, conn);
            if (fileName != null) {
               
                String contentType = Files.probeContentType(Paths.get(fileName));
                response.setContentType(contentType);

               
                if (contentType.startsWith("text/") || contentType.startsWith("image/") || contentType.startsWith("video/") || contentType.equals("application/pdf")) {
                    response.setHeader("Content-Disposition", "inline; filename=\"" + Paths.get(fileName).getFileName().toString() + "\"");
                } else {
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + Paths.get(fileName).getFileName().toString() + "\"");
                }

               
                decryptFileToResponse(fileName, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException | GeneralSecurityException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getFileName(int fileId, Connection conn) throws SQLException {
        String query = "SELECT file_path FROM files WHERE file_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("file_path");
            } else {
                throw new SQLException("File not found for ID: " + fileId);
            }
        }
    }

    private void decryptFileToResponse(String fileName, HttpServletResponse response) throws IOException, GeneralSecurityException {
        try (CustomArchiveReader archiveReader = new CustomArchiveReader(ARCHIVE_PATH);
             CipherInputStream cis = new CipherInputStream(archiveReader.getFileStream(fileName), getCipher(Cipher.DECRYPT_MODE));
             ServletOutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private Cipher getCipher(int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey secretKey = getSecretKey();
        cipher.init(mode, secretKey);
        return cipher;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
