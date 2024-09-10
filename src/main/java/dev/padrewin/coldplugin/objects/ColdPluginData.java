package dev.padrewin.coldplugin.objects;

public class ColdPluginData {

    public final String name;
    public final String version;
    public final String updateVersion;
    public final String website;
    public final String coldPluginVersion;

    public ColdPluginData(String name,
                          String version,
                          String updateVersion,
                          String website,
                          String coldPluginVersion) {
        this.name = name;
        this.version = version;
        this.updateVersion = updateVersion;
        this.website = website;
        this.coldPluginVersion = coldPluginVersion;
    }

    public String name() {
        return this.name;
    }

    public String version() {
        return this.version;
    }

    public String updateVersion() {
        return this.updateVersion;
    }

    public String website() {
        return this.website;
    }

    public String coldPluginVersion() {
        return this.coldPluginVersion;
    }

}