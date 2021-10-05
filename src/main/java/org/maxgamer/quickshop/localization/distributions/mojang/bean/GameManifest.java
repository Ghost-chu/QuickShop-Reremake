package org.maxgamer.quickshop.localization.distributions.mojang.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GameManifest {

    @SerializedName("arguments")
    private ArgumentsDTO arguments;
    @SerializedName("assetIndex")
    private AssetIndexDTO assetIndex;
    @SerializedName("assets")
    private String assets;
    @SerializedName("complianceLevel")
    private Integer complianceLevel;
    @SerializedName("downloads")
    private DownloadsDTO downloads;
    @SerializedName("id")
    private String id;
    @SerializedName("javaVersion")
    private JavaVersionDTO javaVersion;
    @SerializedName("libraries")
    private List<LibrariesDTO> libraries;
    @SerializedName("logging")
    private LoggingDTO logging;
    @SerializedName("mainClass")
    private String mainClass;
    @SerializedName("minimumLauncherVersion")
    private Integer minimumLauncherVersion;
    @SerializedName("releaseTime")
    private String releaseTime;
    @SerializedName("time")
    private String time;
    @SerializedName("type")
    private String type;

    @NoArgsConstructor
    @Data
    public static class ArgumentsDTO {
        @SerializedName("game")
        private List<String> game;
        @SerializedName("jvm")
        private List<JvmDTO> jvm;

        @NoArgsConstructor
        @Data
        public static class JvmDTO {
            @SerializedName("rules")
            private List<RulesDTO> rules;
            @SerializedName("value")
            private List<String> value;

            @NoArgsConstructor
            @Data
            public static class RulesDTO {
                @SerializedName("action")
                private String action;
                @SerializedName("os")
                private OsDTO os;

                @NoArgsConstructor
                @Data
                public static class OsDTO {
                    @SerializedName("name")
                    private String name;
                }
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class AssetIndexDTO {
        @SerializedName("id")
        private String id;
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private Integer size;
        @SerializedName("totalSize")
        private Integer totalSize;
        @SerializedName("url")
        private String url;
    }

    @NoArgsConstructor
    @Data
    public static class DownloadsDTO {
        @SerializedName("client")
        private ClientDTO client;
        @SerializedName("client_mappings")
        private ClientMappingsDTO clientMappings;
        @SerializedName("server")
        private ServerDTO server;
        @SerializedName("server_mappings")
        private ServerMappingsDTO serverMappings;

        @NoArgsConstructor
        @Data
        public static class ClientDTO {
            @SerializedName("sha1")
            private String sha1;
            @SerializedName("size")
            private Integer size;
            @SerializedName("url")
            private String url;
        }

        @NoArgsConstructor
        @Data
        public static class ClientMappingsDTO {
            @SerializedName("sha1")
            private String sha1;
            @SerializedName("size")
            private Integer size;
            @SerializedName("url")
            private String url;
        }

        @NoArgsConstructor
        @Data
        public static class ServerDTO {
            @SerializedName("sha1")
            private String sha1;
            @SerializedName("size")
            private Integer size;
            @SerializedName("url")
            private String url;
        }

        @NoArgsConstructor
        @Data
        public static class ServerMappingsDTO {
            @SerializedName("sha1")
            private String sha1;
            @SerializedName("size")
            private Integer size;
            @SerializedName("url")
            private String url;
        }
    }

    @NoArgsConstructor
    @Data
    public static class JavaVersionDTO {
        @SerializedName("component")
        private String component;
        @SerializedName("majorVersion")
        private Integer majorVersion;
    }

    @NoArgsConstructor
    @Data
    public static class LoggingDTO {
        @SerializedName("client")
        private ClientDTO client;

        @NoArgsConstructor
        @Data
        public static class ClientDTO {
            @SerializedName("argument")
            private String argument;
            @SerializedName("file")
            private FileDTO file;
            @SerializedName("type")
            private String type;

            @NoArgsConstructor
            @Data
            public static class FileDTO {
                @SerializedName("id")
                private String id;
                @SerializedName("sha1")
                private String sha1;
                @SerializedName("size")
                private Integer size;
                @SerializedName("url")
                private String url;
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class LibrariesDTO {
        @SerializedName("downloads")
        private DownloadsDTO downloads;
        @SerializedName("name")
        private String name;
        @SerializedName("rules")
        private List<RulesDTO> rules;
        @SerializedName("natives")
        private NativesDTO natives;
        @SerializedName("extract")
        private ExtractDTO extract;

        @NoArgsConstructor
        @Data
        public static class DownloadsDTO {
            @SerializedName("artifact")
            private ArtifactDTO artifact;

            @NoArgsConstructor
            @Data
            public static class ArtifactDTO {
                @SerializedName("path")
                private String path;
                @SerializedName("sha1")
                private String sha1;
                @SerializedName("size")
                private Integer size;
                @SerializedName("url")
                private String url;
            }
        }

        @NoArgsConstructor
        @Data
        public static class NativesDTO {
            @SerializedName("osx")
            private String osx;
        }

        @NoArgsConstructor
        @Data
        public static class ExtractDTO {
            @SerializedName("exclude")
            private List<String> exclude;
        }

        @NoArgsConstructor
        @Data
        public static class RulesDTO {
            @SerializedName("action")
            private String action;
            @SerializedName("os")
            private OsDTO os;

            @NoArgsConstructor
            @Data
            public static class OsDTO {
                @SerializedName("name")
                private String name;
            }
        }
    }
}
