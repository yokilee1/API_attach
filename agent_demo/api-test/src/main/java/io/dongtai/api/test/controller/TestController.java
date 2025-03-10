package io.dongtai.api.test.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from API Test!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return "Echo: " + message;
    }

    @GetMapping("/user/{id}")
    public String getUser(@PathVariable("id") Long id, @RequestParam(required = false) String fields) {
        return "User " + id + " details" + (fields != null ? " (fields: " + fields + ")" : "");
    }
}