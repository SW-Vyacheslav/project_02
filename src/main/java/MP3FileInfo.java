import com.sun.istack.internal.Nullable;

import java.util.Objects;

public class MP3FileInfo implements Comparable {
    private String artist;
    private String album;
    private String trackName;
    private String duration;
    private String checksum;
    private String fileUrl;

    public MP3FileInfo() {
        artist = "";
        album = "";
        trackName = "";
    }

    public MP3FileInfo(String artist, String album, String trackName) {
        this.artist = artist == null ? "" : artist;
        this.album = album == null ? "" : album;
        this.trackName = trackName == null ? "" : trackName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        if (artist == null) return;
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        if (album == null) return;
        this.album = album;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        if (trackName == null) return;
        this.trackName = trackName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MP3FileInfo that = (MP3FileInfo) o;
        return Objects.equals(artist, that.artist) &&
                Objects.equals(album, that.album) &&
                Objects.equals(trackName, that.trackName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist, album, trackName);
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || getClass() != o.getClass()) return 1;
        MP3FileInfo that = (MP3FileInfo) o;
        int result = artist.compareTo(that.artist);
        if (result == 0) result = album.compareTo(that.album);
        if (result == 0) result = trackName.compareTo(that.trackName);
        return result;
    }
}
