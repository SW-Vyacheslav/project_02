import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> catalogsToScan = new ArrayList<>();
        catalogsToScan.add("C:/Users/Vuacheslav/Music");
        catalogsToScan.add("C:/Users/Vuacheslav/Downloads");

        List<String> catalogsToDelete = new ArrayList<>();
        for (String catalog : catalogsToScan) {
            if (Files.notExists(Paths.get(catalog))) {
                catalogsToDelete.add(catalog);
                System.err.println(String.format("\"%s\" catalog does not exist", catalog));
            }
        }
        catalogsToScan.removeAll(catalogsToDelete);

        MP3FileCataloger cataloger = new MP3FileCataloger(catalogsToScan);
        try {
            cataloger.saveFilesInfosAsHTML();
            cataloger.saveChecksumDuplicatesAsHTML();
            cataloger.saveTagsDuplicatesAsHTML();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
