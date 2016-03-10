package io.github.thisisnozaku.charactercreator.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception generated when attempting to view a Character that isn't available.
 * Created by Damien on 12/7/2015.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MissingCharacterException extends RuntimeException {

}
