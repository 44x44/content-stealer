package dev.voroby.springframework.telegram.model;

import lombok.Data;

import java.util.List;

@Data
public class AddChannelPoolRequest {
    private Long clientId;
    private String schedule;
    private List<String> inviteLinks;
}
