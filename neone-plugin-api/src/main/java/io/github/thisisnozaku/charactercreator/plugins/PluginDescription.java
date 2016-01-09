package io.github.thisisnozaku.charactercreator.plugins;

/**
 * Created by Damien on 11/25/2015.
 */
public class PluginDescription {
    private final String authorName;
    private final String systemName;
    private final String version;

    public String getVersion() {
        return version;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public PluginDescription(String authorName, String systemName, String version) {
        if (authorName == null) {
            throw new IllegalStateException("author name cannot be null");
        }
        if (systemName == null) {
            throw new IllegalStateException("system name cannot be null");
        }
        if (version == null) {
            throw new IllegalStateException("version cannot be null");
        }
        this.authorName = authorName;
        this.systemName = systemName;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginDescription that = (PluginDescription) o;

        if (!authorName.equals(that.authorName)) return false;
        if (!systemName.equals(that.systemName)) return false;
        return version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = authorName.hashCode();
        result = 31 * result + systemName.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PluginDescription{" +
                "authorName='" + authorName + '\'' +
                ", systemName='" + systemName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
