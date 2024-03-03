package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.model.request.AddChannelPoolRequest;
import dev.voroby.springframework.telegram.model.request.TryJoinChannelPoolRequest;
import dev.voroby.springframework.telegram.service.TelegramClientService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class GeneralController {
    @Autowired
    private TelegramClientService telegramClientService;
    @PostMapping(value = "/tryJoinChannelPool", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addChannelPool(@RequestBody TryJoinChannelPoolRequest request) {
        if (!CollectionUtils.isEmpty(request.getIds())) {
            for (Long chatId : request.getIds()) {
                telegramClientService.tryJoinChat(chatId);
            }
        }
        if (!CollectionUtils.isEmpty(request.getInviteLinks())) {
            for (String link : request.getInviteLinks()) {
                telegramClientService.tryJoinChat(link);
            }
        }
    }
}
