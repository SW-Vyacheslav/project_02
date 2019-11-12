import com.mpatric.mp3agic.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MP3FileCataloger {
    private final String filesDir = "./scan_output/";
    private final String firstFilePath = "./scan_output/first.html";
    private String scanCatalog;

    public MP3FileCataloger(String scanCatalog) {
        this.scanCatalog = scanCatalog;
    }

    private List<String> getMP3FilesPaths() {
        List<String> result = null;
        try (Stream<Path> walk = Files.walk(Paths.get(scanCatalog))) {
            result = walk.map(path -> path.toString()).filter(s -> s.endsWith(".mp3")).collect(Collectors.toList());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getFileChecksum(String filepath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream fis = new FileInputStream(filepath)) {
            byte[] buffer = new byte[1024];
            int nRead;
            while ((nRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nRead);
            }
        }
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public void scan() {
        List<String> mp3FilesPaths = getMP3FilesPaths();
        try {
            if (Files.notExists(Paths.get(filesDir)))
                Files.createDirectory(Paths.get(filesDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
