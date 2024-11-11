import java.io.*;
import java.nio.file.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

@MultipartConfig
public class UploadFileServlet extends HttpServlet {

    private static final String BASE_DIRECTORY = "C:\\Users\\LOKESH KUMAR\\OneDrive\\Desktop\\Dummy";
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "mySuperSecretKey";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
	Integer userId = (Integer)session.getAttribute("userId");
	if(userId == null)
	{
	  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	  return;
	}

        
        String username = session.getAttribute("username").toString();
       

        Collection<Part> fileParts = request.getParts();
        String folderIdStr = request.getParameter("folderId");
        int folderId = folderIdStr != null ? Integer.parseInt(folderIdStr) : 0;


        String archiveFilePath = Paths.get(BASE_DIRECTORY, "customArchive.bin").toString();
        
        try (Connection conn = Database.getObject().getConnection();
             CustomArchiveWriter archiveWriter = new CustomArchiveWriter(archiveFilePath)) {

            for (Part filePart : fileParts) {
                if (filePart.getName().equals("file") && filePart.getSize() > 0) {
                    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                    String uniqueFileName = generateUniqueFileName(fileName);
                    String filePath = Paths.get(BASE_DIRECTORY, uniqueFileName).toString();
                    long fileSize = filePart.getSize();
                    boolean isEncrypted = true; 

                    int actualFolderId = folderId;
                    if (actualFolderId == 0) {
                        actualFolderId = getDefaultFolderId(username, userId, conn);
                    }

                   
                    saveFileTemporarily(filePart, filePath);
                    archiveWriter.addFile(filePath);

                   
                    insertFileIntoDatabase(fileName, actualFolderId, userId, uniqueFileName, fileSize, isEncrypted, conn);

                    
                    new java.io.File(filePath).delete();
                } else {
                    System.out.println("No valid file part found: " + filePart.getName());
                }
            }

            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true}");
        } catch (SQLException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void insertFileIntoDatabase(String fileName, int folderId, int userId, String filePath, long fileSize, boolean isEncrypted, Connection conn) throws SQLException {
        System.out.println("In the insert File into database method");
        String query = "INSERT INTO files (name, folder_id, user_id, file_path, file_size, is_encrypted) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fileName);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            stmt.setString(4, filePath);
            stmt.setLong(5, fileSize);
            stmt.setBoolean(6, isEncrypted);
            stmt.executeUpdate();
        }
    }

    private void saveFileTemporarily(Part filePart, String filePath) throws IOException, GeneralSecurityException {
       
        try (FileOutputStream fos = new FileOutputStream(filePath);
             CipherOutputStream cos = new CipherOutputStream(fos, getCipher(Cipher.ENCRYPT_MODE));
             InputStream fileContent = filePart.getInputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileContent.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
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

    private int getDefaultFolderId(String username, int userId, Connection conn) throws SQLException {
        
        String query = "SELECT folder_id FROM folders WHERE user_id = ? AND name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("folder_id");
            } else {
                throw new SQLException("Default folder not found for user");
            }
        }
    }

    private String generateUniqueFileName(String fileName) {
        String uuid = UUID.randomUUID().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName + "_" + uuid;
        } else {
            String name = fileName.substring(0, dotIndex);
            String extension = fileName.substring(dotIndex);
            return name + "_" + uuid + extension;
        }
    }
}

