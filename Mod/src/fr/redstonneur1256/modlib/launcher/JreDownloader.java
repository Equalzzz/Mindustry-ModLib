package fr.redstonneur1256.modlib.launcher;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.*;
import mindustry.Vars;
import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

public class JreDownloader {
    public static class DownloadStatus {
        public static volatile String errMessage = null;
        public static volatile float installationProgress = 0f;
        public static volatile State state = State.NotStarted;
        public enum State {
            NotStarted,
            Pending,
            Downloading,
            Unzipping,
            ConnectionError,
            DownloadingError,
            IOError,
            Success
        }

        public static boolean isDownloading() {
            return state == State.Downloading || state == State.Pending || state == State.NotStarted || state == State.Unzipping;
        }
        public static boolean isError() {
            return state == State.ConnectionError || state == State.DownloadingError;
        }
        public static boolean isSuccess() {
            return state == State.Success;
        }
    }

    private static String getOS() {
        if (OS.isWindows) return "windows";
        if (OS.isMac) return "mac";
        return "linux";
    }

    private static String getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        return (arch.contains("aarch64") || arch.contains("arm")) ? "aarch64" : "x64";
    }

    public static boolean isJreDownloaded() {
        Fi jreDir = Vars.dataDirectory.child("modlib-jre");
        // I can't be bothered enough to make normal check
        String exeName = "jdk-25.0.3+9-jre/bin/java" + (OS.isWindows ? ".exe" : "");
        Fi javaExe = jreDir.child(exeName);
        return javaExe.exists();
    }

    public static void checkAndDownloadJRE() throws Exception {
        // Check whether mod-lib jre exists
        Fi destFolder = Vars.dataDirectory.child("modlib-jre");
        // I can't be bothered enough to make normal check
        String exeName = "jdk-25.0.3+9-jre/bin/java" + (OS.isWindows ? ".exe" : "");
        Fi javaExe = destFolder.child(exeName);
        if (javaExe.exists())
            return;

        // Getting official JRE 25 release download link (https://github.com/adoptium/api.adoptium.net/blob/main/docs/cookbook.adoc)
        String url = "https://api.adoptium.net/v3/binary/latest/25/ga/%s/%s/jre/hotspot/normal/eclipse".formatted(getOS(), getArch());
        Log.info("[ModLib] No mod-lib JRE found. Downloading...\n" + url);

        // Downloading archive to tmp
        Fi archiveFile = Vars.tmpDirectory.child("modlib-jre-download" + (OS.isWindows ? ".zip" : ".tar.gz"));
        DownloadStatus.state = DownloadStatus.State.Pending;
        Http.request(Http.HttpMethod.GET, url).submit(response -> {
            // If we got connection error, stop downloading routine
            if (response.getStatus() != Http.HttpStatus.OK) {
                Log.err("[ModLib] Error downloading JRE: " + response.getStatus());
                DownloadStatus.state = DownloadStatus.State.ConnectionError;
                DownloadStatus.errMessage = "Error downloading JRE: " + response.getStatus();
                return;
            }

            // Downloading archive via 8Kb batches
            DownloadStatus.state = DownloadStatus.State.Downloading;
            try (InputStream is = response.getResultAsStream();
                 OutputStream os = archiveFile.write(false)) {
                long totalBytes = response.getContentLength();

                byte[] buffer = new byte[8192]; // 8 Kb batch
                long totalBytesDownloaded = 0;
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    totalBytesDownloaded += bytesRead;
                    float progress = (float) totalBytesDownloaded / totalBytes;
                    Log.info("[ModLib] Downloading " + progress * 100 + "%");
                    DownloadStatus.installationProgress = progress;
                }
            } catch (Exception e) {
                Log.err("[ModLib] Error downloading JRE: " + e.getMessage());
                DownloadStatus.state = DownloadStatus.State.DownloadingError;
                DownloadStatus.errMessage = "Error downloading JRE: " + e.getMessage();
                return;
            }

            // Extracting downloaded archive
            DownloadStatus.state = DownloadStatus.State.Unzipping;
            try {
                if (OS.isWindows)
                    unzip(archiveFile, destFolder);
                else
                    untarGz(archiveFile, destFolder);
                DownloadStatus.state = DownloadStatus.State.Success;
                Log.info("[ModLib] JRE download complete");
            } catch (Exception e) {
                Log.err("[ModLib] Error unzipping JRE: " + e.getMessage());
                DownloadStatus.state = DownloadStatus.State.IOError;
                DownloadStatus.errMessage = "Error unzipping JRE: " + e.getMessage();
            }
            Log.info("[ModLib] Cleaning up...");
            // IT DOESN'T WORK, I DON'T KNOW HOW TO DELETE IT PLEASE HELP
            Files.delete(archiveFile.file().toPath());
            Log.err("[ModLib] Failed to delete downloaded archive from game's data /tmp directory");
        });
    }

    // I don't know if it works
    private static void untarGz(Fi tarGzFile, Fi destDir) throws IOException {
        try (InputStream fis = tarGzFile.read();
             BufferedInputStream bis = new BufferedInputStream(fis);
             GZIPInputStream gzis = new GZIPInputStream(bis)) {

            byte[] header = new byte[512];
            while (gzis.read(header) == 512) {
                boolean allZero = true;
                for (byte b : header) {
                    if (b != 0) { allZero = false; break; }
                }
                if (allZero) continue;

                String name = new String(header, 0, 100).trim();
                if (name.isEmpty()) continue;

                String sizeStr = new String(header, 124, 12).trim();
                long size = Long.parseLong(sizeStr, 8);
                boolean isDirectory = header[156] == '5';

                Fi file = destDir.child(name);
                if (isDirectory) {
                    file.mkdirs();
                } else {
                    file.parent().mkdirs();
                    try (OutputStream os = file.write(false)) {
                        byte[] buffer = new byte[4096];
                        long remaining = size;
                        while (remaining > 0) {
                            int readLen = (int) Math.min(buffer.length, remaining);
                            int bytesRead = gzis.read(buffer, 0, readLen);
                            if (bytesRead == -1) break;
                            os.write(buffer, 0, bytesRead);
                            remaining -= bytesRead;
                        }
                    }
                }
                long offset = (512 - (size % 512)) % 512;
                gzis.skip(offset);
            }
        }
    }

    private static void unzip(Fi zipFile, Fi destFolder) {
        Log.info("[ModLib] Unzipping JRE...");
        if (!zipFile.exists() || zipFile.length() == 0) {
            Log.err("[ModLib] Extraction aborted: Zip file does not exist or is 0 Kb. Current size: @ bytes", zipFile.length());
            return;
        }

        ZipFi zipRoot = new ZipFi(zipFile);

        // Create or clear modlib-jre folder if missing
        if (destFolder.exists())
            destFolder.deleteDirectory();
        destFolder.mkdirs();

        zipRoot.copyTo(destFolder);
    }
}
