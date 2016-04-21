package io.github.thisisnozaku.charactercreator.data;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;

import java.math.BigInteger;

/**
 * Created by Damien on 2/4/2016.
 */
public class CharacterDataWrapper {
    private String id;
    private PluginDescription plugin;
    private User user;
    @JsonRawValue
    private String character;

    public CharacterDataWrapper() {
    }


    public CharacterDataWrapper(PluginDescription pluginDescription, User user, String character) {
        this.plugin = pluginDescription;
        this.user = user;
        this.character = character;
    }

    public PluginDescription getPlugin() {
        return plugin;
    }

    public User getUser() {
        return user;
    }

    public String getCharacter() {
        return character;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CharacterDataWrapper wrapper = (CharacterDataWrapper) o;

        if (id != null ? !id.equals(wrapper.id) : wrapper.id != null) return false;
        if (plugin != null ? !plugin.equals(wrapper.plugin) : wrapper.plugin != null)
            return false;
        if (user != null ? !user.equals(wrapper.user) : wrapper.user != null) return false;
        return character != null ? character.equals(wrapper.character) : wrapper.character == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (plugin != null ? plugin.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (character != null ? character.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CharacterDataWrapper{" +
                "id=" + id +
                ", plugin=" + plugin +
                ", user=" + user +
                ", character=" + character +
                '}';
    }
}
