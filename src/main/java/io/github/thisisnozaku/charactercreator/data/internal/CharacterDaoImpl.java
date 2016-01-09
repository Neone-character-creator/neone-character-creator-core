package io.github.thisisnozaku.charactercreator.data.internal;

import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.data.CharacterDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Damien on 11/29/2015.
 */
@Repository
public class CharacterDaoImpl implements CharacterDao {

    @Override
    public <T extends Character> T createCharacter(T character) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends Character> Optional<T> getCharacter(long id, Class<T> typeObject) {
        return null;
    }

    @Override
    public void deleteCharacter(long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> T updateCharacter(T character) throws IllegalStateException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
