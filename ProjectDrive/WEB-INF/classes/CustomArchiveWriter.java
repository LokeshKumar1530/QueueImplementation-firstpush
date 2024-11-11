import java.io.*;

public class CustomArchiveWriter implements Closeable {
    private DataOutputStream dos;

    public CustomArchiveWriter(String archiveFileName) throws IOException {
        this.dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(archiveFileName, true)));
    }

    public void addFile(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            String fileName = file.getName();
            long fileSize = file.length();

            dos.writeUTF(fileName);
            dos.writeLong(fileSize);


            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (dos != null) {
            dos.close();
        }
    }

}
