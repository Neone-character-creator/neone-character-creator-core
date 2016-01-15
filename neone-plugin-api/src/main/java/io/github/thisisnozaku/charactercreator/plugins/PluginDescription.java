package io.github.thisisnozaku.charactercreator.plugins;

/**
 * Created by Damien on 11/25/2015.
 */
public class PluginDescription {
    private String author;
    private String system;
    private String version;

    public String getVersion() {
        return version;
    }

    public String getSystem() {
        return system;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PluginDescription(String author, String system, String version) {
        if (author == null) {
            throw new IllegalStateException("author name cannot be null");
        }
        if (system == null) {
            throw new IllegalStateException("system name cannot be null");
        }
        if (version == null) {
            throw new IllegalStateException("version cannot be null");
        }
        this.author = author;
        this.system = system;
        this.version = version;
    }

    public PluginDescription() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginDescription that = (PluginDescription) o;

        if (!author.equals(that.author)) return false;
        if (!system.equals(that.system)) return false;
        return version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + system.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PluginDescription{" +
                "author='" + author + '\'' +
                ", system='" + system + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
