package com.bjoernkw.schematic;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("schematic")
public class SchematicProperties implements InitializingBean {

    private String name;

    private String version = getClass().getPackage().getImplementationVersion();

    private String path = "schematic";

    private String rootPath = "/";

    private int previewRowLimit = 10;

    @Override
    public void afterPropertiesSet() {
        if (previewRowLimit < 1) {
            throw new IllegalStateException(
                    "schematic.preview-row-limit must be a positive integer but was: " + previewRowLimit
            );
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public int getPreviewRowLimit() {
        return previewRowLimit;
    }

    public void setPreviewRowLimit(int previewRowLimit) {
        this.previewRowLimit = previewRowLimit;
    }
}