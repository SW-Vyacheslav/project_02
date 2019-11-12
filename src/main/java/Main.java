import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Write directory path to scan:");
        String scanDir = scanner.nextLine();

        if (Files.exists(Paths.get(scanDir))) {
            MP3FileCataloger cataloger = new MP3FileCataloger(scanDir);
            cataloger.scan();
        } else {
            System.out.println("This directory does not exist");
        }
    }
}
