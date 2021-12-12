package com.journaldev.rediscachedemo.expose;

import com.journaldev.rediscachedemo.model.User;
import com.journaldev.rediscachedemo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "users")
public class UserController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "user", key = "#userId")
    @GetMapping("/{userId}")
    public User getUser(@PathVariable String userId) {
        LOG.info("Getting user with ID {}.", userId);
        User user = null;
        Optional<User> userResponse = userRepository.findById(Long.valueOf(userId));
        if (userResponse.isPresent()) {
            user = userResponse.get();
        } else {
            throw new RuntimeException("Record not found");
        }
        return user;
    }

    @CachePut(value = "user", key = "#user.id")
    @PutMapping("/update")
    public User updatePersonByID(@RequestBody User user) {
        User updateResponse = null;
        Optional<User> userResponse = userRepository.findById(user.getId());
        if (userResponse.isPresent()) {
            User u = userResponse.get();
            u.setName(user.getName());
            u.setFollowers(user.getFollowers());
            updateResponse = userRepository.save(u);
        } else {
            updateResponse = userRepository.save(user);
        }
        return updateResponse;
    }

    @CacheEvict(value = "user", key = "#user.id", allEntries=true)
    @DeleteMapping("/delete/{userId}")
    public void deleteUserByID(@PathVariable Long userId) {
        LOG.info("deleting person with id {}", userId);
        userRepository.deleteById(userId);
    }
}
