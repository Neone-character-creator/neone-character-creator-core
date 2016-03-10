package io.github.thisisnozaku.charactercreator.authentication;

import io.github.thisisnozaku.charactercreator.data.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by Damien on 2/13/2016.
 */
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService{
    @Inject
    UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException(username);
        }
        return user;
    }
}
