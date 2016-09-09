package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Damien on 9/2/2016.
 */
public interface CharacterActivationTokenRepository extends JpaRepository<ActivationToken, String>{
}
