package dev.voroby.springframework.telegram.model.request;

import lombok.Data;

import java.util.List;

@Data
public class TryJoinChannelPoolRequest {
    private List<Long> ids;
    private List<String> inviteLinks;
}
