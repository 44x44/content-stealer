package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.model.AddChannelPoolRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/client")
public class ClientController {
    @PostMapping(value = "/addChannelPool", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addChannelPool(@RequestBody AddChannelPoolRequest request) {

    }
}
