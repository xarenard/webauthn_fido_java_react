package org.orquanet.service;

import io.swagger.annotations.Contact;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orquanet.webauthn.repository.UserRepository;
import org.orquanet.webauthn.repository.model.User;
import org.orquanet.webauthn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Autowired
    @InjectMocks
    private UserService userService;

    @Test
    public void testFindByEmail(){
        User user = User.builder().firstName("Xa").lastName("Ren").id(1L).email("x@h.com").build();
        Mockito.when(userRepository.findUserByMail("x@h.com")).thenReturn(Optional.of(user));
        Optional<User> optionalUser = userService.findUserByEmail("x@h.com");
        Assert.assertTrue(optionalUser.isPresent());
    }

}
