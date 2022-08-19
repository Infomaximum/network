package com.infomaximum.network.transport.http.builder;

import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUploadFiles {

    private final Path location; //the directory location where files will be stored
    private final long maxFileSize; //the maximum size allowed for uploaded files
    private final long maxRequestSize; //the maximum size allowed for multipart/form-data requests
    private final int fileSizeThreshold; //the size threshold after which files will be written to disk

    private ConfigUploadFiles(Builder builder) {
        this.location = builder.location;
        this.maxFileSize = builder.maxFileSize;
        this.maxRequestSize = builder.maxRequestSize;
        this.fileSizeThreshold = builder.fileSizeThreshold;
    }

    /**
     * Gets the directory location where files will be stored.
     *
     * @return the directory location where files will be stored
     */
    public Path getLocation() {
        return this.location;
    }

    /**
     * Gets the maximum size allowed for uploaded files.
     *
     * @return the maximum size allowed for uploaded files
     */
    public long getMaxFileSize() {
        return this.maxFileSize;
    }

    /**
     * Gets the maximum size allowed for multipart/form-data requests.
     *
     * @return the maximum size allowed for multipart/form-data requests
     */
    public long getMaxRequestSize() {
        return this.maxRequestSize;
    }

    /**
     * Gets the size threshold after which files will be written to disk.
     *
     * @return the size threshold after which files will be written to disk
     */
    public int getFileSizeThreshold() {
        return this.fileSizeThreshold;
    }

    public static class Builder {

        private Path location;
        private long maxFileSize;
        private long maxRequestSize;
        private int fileSizeThreshold;

        public Builder() {
            this.location = Path.of(System.getProperty("java.io.tmpdir"));
            this.maxFileSize = this.maxRequestSize = DataSize.ofMegabytes(32).toBytes();
            this.fileSizeThreshold = (int) DataSize.ofKilobytes(10).toBytes();
        }

        /**
         * Место положения закаченных файлов(если не помещаются в лимит fileSizeThreshold)
         * @param location
         * @return
         */
        public Builder withLocation(Path location) {
            this.location = location;
            return this;
        }

        /**
         * Максимальный размер загружаемого одного файлы
         * @param maxFileSize
         * @return
         */
        public Builder withMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        /**
         * Максимальный размер всех загружаемых файлов
         * @param maxRequestSize
         * @return
         */
        public Builder withMaxRequestSize(long maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
            return this;
        }

        /**
         * Предел размера файлов после которого будут сохранены на диске
         * @param fileSizeThreshold
         * @return
         */
        public Builder withFileSizeThreshold(int fileSizeThreshold) {
            this.fileSizeThreshold = fileSizeThreshold;
            return this;
        }

        public ConfigUploadFiles build() {
            return build(false);
        }

        public ConfigUploadFiles build(boolean isCreateLocationIfNeed) {
            if (!Files.exists(location)) {
                if (isCreateLocationIfNeed) {
                    try {
                        Files.createDirectories(location);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (!Files.isDirectory(location)) {
                throw new IllegalArgumentException("Path is not directory:" + location.toAbsolutePath().toString());
            }

            return new ConfigUploadFiles(this);
        }
    }
}
