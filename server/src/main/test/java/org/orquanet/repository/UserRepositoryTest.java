package org.orquanet.repository;

import org.junit.Assert;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.orquanet.webauthn.repository.UserRepository;
import org.orquanet.webauthn.repository.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;


@DataJpaTest()
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    @Test
    @Order(1)
    @Rollback(false)
    public void testUserSave(){
        System.out.println(userRepository);
        User user = User.builder().lastName("Renard").firstName("Xavier").email("x@hh.com").build();
        User insertedUser = userRepository.save(user);
        Assert.assertNotNull(insertedUser);
        Assert.assertNotNull(insertedUser.getId());
    }

    @Test
    @Order(2)

    public void testUserGet(){
        User user = User.builder().lastName("Renard").firstName("Xavier").email("x@hh.com").build();
        userRepository.save(user);
        Optional<User> userOptional = userRepository.findUserByMail("x@hh.com");
        Boolean userPresent = userOptional.isPresent();
        Assert.assertTrue(userPresent);
    }
}
