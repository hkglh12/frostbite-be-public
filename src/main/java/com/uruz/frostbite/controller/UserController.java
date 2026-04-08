package com.uruz.frostbite.controller;

import com.uruz.frostbite.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }
    @PostMapping(value="/")
    public ResponseEntity<?> postUser(){
        return null;
    }

    @GetMapping(value="/")
    public ResponseEntity<?> getUser(){

        return null;
    }

    @DeleteMapping(value="/")
    public ResponseEntity<?> deleteUser(){
        return null;
    }

    @PutMapping(value="/")
    public ResponseEntity<?> putUser(){

        return null;
    }

}
