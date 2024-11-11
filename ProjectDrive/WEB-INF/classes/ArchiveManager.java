import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ArchiveManager
{
   private static final String ARCHIVE_FILE_NAME = "C:\\Users\\lokesh-inc3954\\Desktop\\Dummy\\customArchive.bin";
 
   public static void deleteFileFromArchive(String fileNameToDelete) throws IOException
   {
      
      File originalFile = new File(ARCHIVE_FILE_NAME);
      File directory = originalFile.getParentFile();      
     
      File tempFile = File.createTempFile("tempArchive", ".bin", directory); 
      tempFile.deleteOnExit(); 
      
      try (FileInputStream fis = new FileInputStream(ARCHIVE_FILE_NAME);
           FileOutputStream fos = new FileOutputStream(tempFile);
           DataInputStream dis = new DataInputStream(fis);
           DataOutputStream dos = new DataOutputStream(fos))
      {
         boolean fileDeleted = false;
         
         
         int availableBytes = dis.available();
         
         while (availableBytes > 0)
         {
            
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            
            
            if (fileName.equals(fileNameToDelete))
            {
               dis.skipBytes((int) fileSize);
               fileDeleted = true;
            }
            else
            {
               dos.writeUTF(fileName);
               dos.writeLong(fileSize);
               byte[] buffer = new byte[1024];
               long bytesRead = 0;
               while (bytesRead < fileSize)
               {
                  int length = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesRead));
                  dos.write(buffer, 0, length);
                  bytesRead += length;
               }
            }
            
           
            availableBytes = dis.available();
         }
         
         if (!fileDeleted)
         {
            System.out.println("File to delete was not found.");
         }
      }
      catch (IOException e)
      {
         System.err.println("Error during file operations: " + e.getMessage());
         e.printStackTrace();
      }
      
      try {
          Path originalPath = Paths.get(ARCHIVE_FILE_NAME);
          Files.move(tempFile.toPath(), originalPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
          System.err.println("Error moving temporary file: " + e.getMessage());
          e.printStackTrace();
      }
   }
}
