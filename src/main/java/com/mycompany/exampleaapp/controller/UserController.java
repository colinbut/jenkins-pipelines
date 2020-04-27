package com.mycompany.exampleaapp.controller;

import com.mycompany.exampleaapp.dto.UserDto;
import com.mycompany.exampleaapp.model.User;
import com.mycompany.exampleaapp.repository.UserRepository;
import com.mycompany.exampleaapp.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users/list")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/user")
    public ResponseEntity<UserDto> getUsersByUsername(@RequestParam(value = "username") String username) {
        User user = userRepository.findByUsername(username);
        UserDto userDto = UserMapper.mapUserToUserDto(user);
        return ResponseEntity.ok(userDto);
    }
}
