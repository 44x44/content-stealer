package dev.voroby.springframework.telegram.model.request;

import lombok.Data;

import java.util.List;

@Data
public class AddChannelPoolRequest {
    private Long clientId;
    private String schedule;
    private List<String> inviteLinks;
}
