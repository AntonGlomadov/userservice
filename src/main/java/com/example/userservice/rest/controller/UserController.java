package com.example.userservice.rest.controller;

import com.example.userservice.db.UserService;
import com.example.userservice.dto.*;
import org.keycloak.KeycloakPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/s/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity create(@RequestBody UserDTO user) {
        userService.addUser(user);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(path = "/s/get/subscribes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShortUserInfoDTO> getUserSubscribes(@RequestBody SubscribeDTO user) {
        return userService.getAllSubscribes(user.getUsername());
    }

    @PostMapping(path = "/profile/subscribe", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity subscribe(@RequestBody SubscribeDTO user,Principal principal) {
        userService.subscribe(user,((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername());
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(path = "/profile/unsubscribe", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity unsubscribe(@RequestBody SubscribeDTO user,Principal principal) {
        userService.unsubscribe(user,((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "/profile/get/subscribes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShortUserInfoDTO> getAllSubscribes(Principal principal) {
        return userService.getAllSubscribes(((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername());
    }

    @GetMapping(path = "/profile/get/subscribers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShortUserInfoDTO> getAllSubscribers(Principal principal) {
        return userService.getAllSubscribers(((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername());
    }

    @GetMapping(path = "/profile/get/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUserInfo(Principal principal) throws IOException {
        return userService.getUserInfo(((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername());
    }

    @PostMapping(path = "/profile/get/user/other", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserandSubStatusDTO getOtherUserInfo(@RequestBody SubscribeDTO user, Principal principal) throws IOException {
        return userService.getUserAndSubStatus(user.getUsername(),((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername());
    }

    @PostMapping(path = "/profile/finduser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShortUserInfoDTO> findUsers(@RequestBody FindDTO findPatern) {
        return userService.findUsers(findPatern);
    }

    @PostMapping(path = "/profile/addPhoto")
    public ResponseEntity addPhoto(@RequestParam("file") MultipartFile data,Principal principal) {
        userService.addPhoto(((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken().getPreferredUsername(),data);
        return new ResponseEntity(HttpStatus.OK);
    }
}
