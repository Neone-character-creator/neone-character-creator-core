package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.plugins.Character;

import java.util.Optional;

/**
 * Created by Damien on 11/22/2015.
 */
public interface CharacterDao {
    /**
     * Retrieves the character object with the given id and of the given type.
     *
     * @param id   the id of the character
     * @param type the class object used to cast the returned value
     * @param <T>  the concrete type of the returned instance
     * @return an Optional containing the character, if it exists or empty otherwise
     */
    <T extends Character> Optional<T> getCharacter(long id, Class<T> type);

    /**
     * Creates a new character object. The system will generate an id for the object and return an object with the new id.
     * This method is not guaranteed to return the same instance of the object that was passed into it.
     *
     * @param character the character
     * @param <T>       the type of the character
     * @return the updated character
     */
    <T extends Character> T createCharacter(T character);

    /**
     * Deletes the character with the given id.
     *
     * @param id the id
     */
    void deleteCharacter(long id);

    /**
     * Updates the character in the database, replacing the character in the database with the same id as the given
     * character object with it.
     *
     * @param character the character to replace.
     * @param <T>       the type of the character to replace
     * @return the updated character, if it exists
     * @throws IllegalStateException if the character doesn't exist
     */
    <T> T updateCharacter(T character) throws IllegalStateException;

}
