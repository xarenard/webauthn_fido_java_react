package org.orquanet.webauthn.controller.user;

import org.orquanet.webauthn.controller.user.dto.UserDto;
import org.orquanet.webauthn.repository.FidoUserRepository;
import org.orquanet.webauthn.repository.UserRepository;
import org.orquanet.webauthn.repository.model.FidoUser;
import org.orquanet.webauthn.repository.model.User;
import org.orquanet.webauthn.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@RestController
public class UserRegistration {

    private SecureRandom secureRandom = new SecureRandom();
    private FidoUserRepository fidoUserRepository;
    private UserService userService;

    public UserRegistration(FidoUserRepository fidoUserRepository, UserService userService){
        this.userService = userService;
        this.fidoUserRepository = fidoUserRepository;
    }

    @CrossOrigin(origins = "${webauthn.origins.allowed}", allowCredentials = "true", methods = {RequestMethod.POST})
    @PostMapping("/registration/user")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<String> register(@RequestBody UserDto userDTO) {

        String email = userDTO.getEmail();
        Optional<User> optionalUser = userService.findUserByEmail(email);
        if(optionalUser.isPresent()){
            return new ResponseEntity<String>(HttpStatus.CONFLICT);
        }
        byte[] randomBytes = new byte[30];
        User user = User.builder().email(userDTO.getEmail()).firstName(userDTO.getFirstName()).lastName(userDTO.getLastName()).build();
        secureRandom.nextBytes(randomBytes);
        FidoUser fidoUser = FidoUser.builder().user(user).fidoId(Base64.getEncoder().encodeToString(randomBytes)).build();
        this.fidoUserRepository.save(fidoUser);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
