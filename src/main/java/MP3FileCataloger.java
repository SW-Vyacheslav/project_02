import com.mpatric.mp3agic.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MP3FileCataloger {
    private final String FILES_DIR = "./scan_output";
    private final String LIST_FILE_PATH = FILES_DIR + "/list.html";
    private final String CHECKSUM_DUPLICATES_FILE_PATH = FILES_DIR + "/checksum_doubles.html";
    private final String TAG_DUPLICATES_FILE_PATH = FILES_DIR + "/tag_doubles.html";

    private List<String> scanCatalogs;
    private List<String> mp3FilesPaths;
    private List<MP3FileInfo> mp3FilesInfos;

    public MP3FileCataloger(List<String> scanCatalogs) {
        this.scanCatalogs = scanCatalogs;
        mp3FilesInfos = new ArrayList<>();
        init();
    }

    private void init() {
        try {
            if (Files.notExists(Paths.get(FILES_DIR)))
                Files.createDirectory(Paths.get(FILES_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp3FilesPaths = getFilesPaths();
        mp3FilesPaths.forEach(s -> {
            MP3FileInfo mp3FileInfo = new MP3FileInfo();
            try {
                Mp3File mp3File = new Mp3File(s);
                if (mp3File.hasId3v2Tag()) {
                    ID3v2 tag = mp3File.getId3v2Tag();
                    mp3FileInfo.setArtist(tag.getArtist());
                    mp3FileInfo.setAlbum(tag.getAlbum());
                    mp3FileInfo.setTrackName(tag.getTitle());
                    int hours = (int)mp3File.getLengthInSeconds() / 3600;
                    int minutes = (int)mp3File.getLengthInSeconds() / 60 - hours * 60;
                    int seconds = (int)mp3File.getLengthInSeconds() - hours * 3600 - minutes * 60;
                    mp3FileInfo.setDuration(String.format("%d:%d:%d", hours, minutes, seconds));
                    mp3FileInfo.setChecksum(getFileChecksum(s));
                    mp3FileInfo.setFileUrl(s);
                    mp3FilesInfos.add(mp3FileInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedTagException e) {
                e.printStackTrace();
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
        });
    }

    private List<String> getFilesPaths() {
        Set<String> result = new HashSet<>();
        for (String catalog : scanCatalogs) {
            try (Stream<Path> walk = Files.walk(Paths.get(catalog))) {
                result.addAll(walk.map(path -> path.toString()).filter(s -> s.endsWith(".mp3")).collect(Collectors.toSet()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(result);
    }

    private String getFileChecksum(String filePath) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int nRead;
            while ((nRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nRead);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private List<MP3FileInfo> findDuplicatesByChecksum() {
        List<String> checksumDuplicates = mp3FilesInfos.stream()
                .collect(Collectors.groupingBy(mp3FileInfo -> mp3FileInfo.getChecksum(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(stringLongEntry -> stringLongEntry.getValue() > 1)
                .map(stringLongEntry -> stringLongEntry.getKey())
                .collect(Collectors.toList());

        return mp3FilesInfos.stream()
                .filter(mp3FileInfo -> checksumDuplicates.stream()
                        .anyMatch(s -> mp3FileInfo.getChecksum().equals(s)))
                .collect(Collectors.toList());
    }

    private List<MP3FileInfo> findDuplicatesByTag() {
        List<MP3FileInfo> tagDuplicates = mp3FilesInfos.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(mp3FileInfoLongEntry -> mp3FileInfoLongEntry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return mp3FilesInfos.stream()
                .filter(mp3FileInfo -> tagDuplicates.stream()
                        .anyMatch(fi -> fi.equals(mp3FileInfo)))
                .collect(Collectors.toList());
    }

    public void saveFilesInfosAsHTML() throws IOException {
        HtmlFile htmlFile = new HtmlFile(LIST_FILE_PATH);

        if (mp3FilesInfos.size() != 0) {
            List<String> artists = mp3FilesInfos.stream()
                    .collect(Collectors.groupingBy(mp3FileInfo -> mp3FileInfo.getArtist()))
                    .entrySet()
                    .stream()
                    .map(stringListEntry -> stringListEntry.getKey())
                    .collect(Collectors.toList());

            for (String artist : artists) {
                htmlFile.addBoldText(artist.isEmpty() ? "No info about artist" : artist, "green");
                List<String> albums = mp3FilesInfos.stream()
                        .filter(mp3FileInfo -> mp3FileInfo.getArtist().equals(artist))
                        .collect(Collectors.groupingBy(mp3FileInfo -> mp3FileInfo.getAlbum()))
                        .entrySet()
                        .stream()
                        .map(stringListEntry -> stringListEntry.getKey())
                        .collect(Collectors.toList());

                for (String album : albums) {
                    htmlFile.addBoldText(album.isEmpty() ? "No info about album" : album, "chocolate");
                    List<MP3FileInfo> albumInfos = mp3FilesInfos.stream()
                            .filter(mp3FileInfo -> mp3FileInfo.getArtist().equals(artist) && mp3FileInfo.getAlbum().equals(album))
                            .collect(Collectors.toList());
                    for (MP3FileInfo fileInfo : albumInfos) {
                        htmlFile.addText(String.format("Track Name: %s , Duration: %s", fileInfo.getTrackName(), fileInfo.getDuration()));
                        htmlFile.addLink(fileInfo.getFileUrl());
                    }
                }
            }

        } else {
            htmlFile.addBoldText("Files not founded");
        }

        htmlFile.save();
    }

    public void saveChecksumDuplicatesAsHTML() throws IOException {
        List<MP3FileInfo> mp3FileInfos = findDuplicatesByChecksum();
        HtmlFile htmlFile = new HtmlFile(CHECKSUM_DUPLICATES_FILE_PATH);

        if (mp3FileInfos.size() != 0) {
            List<MP3FileInfo> mp3FileInfosSorted = mp3FileInfos.stream()
                    .sorted(Comparator.comparing(MP3FileInfo::getChecksum))
                    .collect(Collectors.toList());

            htmlFile.addBoldText("Duplicates 1");
            htmlFile.addLink(mp3FileInfosSorted.get(0).getFileUrl());

            for (int i = 1, j = 2; i < mp3FileInfosSorted.size(); ++i) {
                if (mp3FileInfosSorted.get(i).getChecksum().equals(mp3FileInfosSorted.get(i - 1).getChecksum())) {
                    htmlFile.addLink(mp3FileInfosSorted.get(i).getFileUrl());
                } else {
                    htmlFile.addBoldText(String.format("Duplicates %d", j));
                    htmlFile.addLink(mp3FileInfosSorted.get(i).getFileUrl());
                    ++j;
                }
            }
        } else {
            htmlFile.addBoldText("Duplicates not founded");
        }

        htmlFile.save();
    }

    public void saveTagsDuplicatesAsHTML() throws IOException {
        List<MP3FileInfo> mp3FileInfos = findDuplicatesByTag();
        HtmlFile htmlFile = new HtmlFile(TAG_DUPLICATES_FILE_PATH);

        if (mp3FileInfos.size() != 0) {
            List<MP3FileInfo> mp3FileInfosSorted = mp3FileInfos.stream()
                    .sorted(MP3FileInfo::compareTo)
                    .collect(Collectors.toList());

            htmlFile.addBoldText(String.format("Artist: %s , Album: %s , Track Name: %s",
                    mp3FileInfosSorted.get(0).getArtist(),
                    mp3FileInfosSorted.get(0).getAlbum(),
                    mp3FileInfosSorted.get(0).getTrackName()));
            htmlFile.addLink(mp3FileInfosSorted.get(0).getFileUrl());

            for (int i = 1; i < mp3FileInfosSorted.size(); ++i) {
                if (mp3FileInfosSorted.get(i).compareTo(mp3FileInfosSorted.get(i - 1)) == 0) {
                    htmlFile.addLink(mp3FileInfosSorted.get(i).getFileUrl());
                } else {
                    htmlFile.addBoldText(String.format("Artist: %s , Album: %s , Track Name: %s",
                            mp3FileInfosSorted.get(i).getArtist(),
                            mp3FileInfosSorted.get(i).getAlbum(),
                            mp3FileInfosSorted.get(i).getTrackName()));
                    htmlFile.addLink(mp3FileInfosSorted.get(i).getFileUrl());
                }
            }
        } else {
            htmlFile.addBoldText("Duplicates not founded");
        }

        htmlFile.save();
    }
}
