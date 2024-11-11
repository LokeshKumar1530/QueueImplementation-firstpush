import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class CustomArchiveReader implements Closeable {
    private DataInputStream dis;
    private String archiveFileName;

    public CustomArchiveReader(String archiveFileName) throws IOException {
        System.out.println("In the CustomArchiveReader");
        this.archiveFileName = archiveFileName;
        this.dis = new DataInputStream(new FileInputStream(archiveFileName));
    }

    public InputStream getFileStream(String fileNameToExtract) throws IOException {
        this.dis.close();
        this.dis = new DataInputStream(new FileInputStream(archiveFileName));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean fileFound = false;


        while (dis.available() > 0) {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            if (fileName.equals(fileNameToExtract)) {
               

                byte[] buffer = new byte[1024];
                long bytesRead = 0;
                while (bytesRead < fileSize) {
                    int length = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesRead));
                    output.write(buffer, 0, length);
                    bytesRead += length;
                }
                fileFound = true;
                break;
            } else {
                System.out.println("Skipping file: " + fileName);
                skipFully(dis, fileSize);
            }
        }

        if (!fileFound) {
            throw new FileNotFoundException("File " + fileNameToExtract + " not found in the bin");
        }

        return new ByteArrayInputStream(output.toByteArray());
    }

    private void skipFully(DataInputStream dis, long bytesToSkip) throws IOException {
        long remaining = bytesToSkip;
        while (remaining > 0) {
            long skipped = dis.skip(remaining);
            if (skipped == 0) {
               
                if (dis.read() == -1) {
                    break;
                }
                remaining--;
            } else {
                remaining -= skipped;
            }
        }
    }

    @Override
    public void close() throws IOException {
        dis.close();
    }

   
}



