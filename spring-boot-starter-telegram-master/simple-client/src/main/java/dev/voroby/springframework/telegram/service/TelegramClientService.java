package dev.voroby.springframework.telegram.service;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.repository.AlmightyRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service @Slf4j
public class TelegramClientService {

    @Autowired
    @Lazy
    private TelegramClient telegramClient;
    @Autowired
    private AlmightyRepository almightyRepository;

    private final Deque<TdApi.Message> messages = new ConcurrentLinkedDeque<>();

    public void putMessage(TdApi.Message msg) {
        messages.addLast(msg);
    }

    /*public void addChannelPool(AddChannelPoolRequest request) {
        for (String inviteLink : request.getInviteLinks()) {
            tryJoinChat(inviteLink);
        }
        List<Long> channelIds = getLastChannelPoolIds();
        ChannelPool pool = new ChannelPool()
            .setClientId(request.getClientId())
            .setSchedule(request.getSchedule())
            .setClientChannelId(channelIds.remove(channelIds.size() - 1))
            .setChannelIdsList(channelIds);
        clientRepository.insertChannelPool(pool);
    }*/

    @SneakyThrows
    public void tryJoinChat(String inviteLink) {
        telegramClient.sendSync(new TdApi.JoinChatByInviteLink(inviteLink), TdApi.Ok.class);
    }

    @SneakyThrows
    public void tryJoinChat(Long chatId) {
        telegramClient.sendSync(new TdApi.JoinChat(chatId), TdApi.Ok.class);
    }

    private List<Long> getLastChannelPoolIds() {
        List<Long> result = new ArrayList<>();
        telegramClient.sendSync(new TdApi.GetChatHistory(823105613L, 0, 0, 10, false), TdApi.Messages.class);
        TdApi.Messages messages = telegramClient.sendSync(new TdApi.GetChatHistory(823105613L, 0, 0, 10, false), TdApi.Messages.class);
        Long lastMessageId = null;
        while (true) {
            for (TdApi.Message message : messages.messages) {
                if (message.content instanceof TdApi.MessageText mt && "---".equals(mt.text.text)) {
                    return result;
                }
                if (message.forwardInfo != null && message.forwardInfo.origin instanceof TdApi.MessageForwardOriginChannel) {
                    result.add(((TdApi.MessageForwardOriginChannel) message.forwardInfo.origin).chatId);
                }
                lastMessageId = message.id;
            }
            messages = telegramClient.sendSync(new TdApi.GetChatHistory(823105613L, lastMessageId, 0, 10, false), TdApi.Messages.class);
        }
    }

    /*@Scheduled(fixedDelay = 1000)
    private void handleMessages() {
        for (int i = 0; i < 100; i++) {
            TdApi.Message message = messages.pollFirst();
            if (message == null) {
                break;
            }
            TdApi.MessageContent content = message.content;
            if (content instanceof TdApi.MessageText mt) {
                TdApi.Chat chat = telegramClient.sendSync(new TdApi.GetChat(message.chatId), TdApi.Chat.class);
                log.info("Incoming text message:\n[\n\ttitle: {},\n\tmessage: {}\n]", chat.title, mt.text.text);
            }
        }
    }*/
}
