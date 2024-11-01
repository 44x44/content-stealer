package dev.voroby.springframework.telegram;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import dev.voroby.springframework.telegram.utils.TdApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class TelegramClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApplication.class, args);
    }

    @Autowired
    private TelegramClient telegramClient;

    @Autowired
    private ClientAuthorizationState authorizationState;

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            while (!authorizationState.haveAuthorization()) {
                /*wait for authorization*/
                TimeUnit.MILLISECONDS.sleep(200);
            }
            TdApi.LoadChats loadChatsQuery = new TdApi.LoadChats(new TdApi.ChatListMain(), 100);
            telegramClient.sendWithCallback(loadChatsQuery, this::loadChatsHandler);

            // все, что дальше в этом методе, использовалось исключительно для быстрого теста

            //TdApi.ChatLists chatLists = telegramClient.sendSync(new TdApi.GetChatListsToAddChat(-1001792690419L), TdApi.ChatLists.class);
            //TdApi.Chat chat = telegramClient.sendSync(new TdApi.JoinChatByInviteLink("https://t.me/+oDf_lVJzbNQyYWFi"), TdApi.Chat.class);
            //var chats = telegramClient.sendSync(new TdApi.GetChats(new TdApi.ChatListMain(), 100));
            //TdApi.ChatFolder folder = telegramClient.sendSync(new TdApi.GetChatFolder(5), TdApi.ChatFolder.class);
            //TdApi.Ok resp = telegramClient.sendSync(new TdApi.JoinChat(-1001633241826L), TdApi.Ok.class);
            //17207132160
            //17208180736
            //17209229312

            //telegramClient.sendSync(new TdApi.GetChatHistory(-1001077837216L, 0, 0, 10, false), TdApi.Messages.class);
            //TdApi.Messages messages = telegramClient.sendSync(new TdApi.GetChatHistory(-1001077837216L, 0, 0, 10, false), TdApi.Messages.class);
            /*telegramClient.sendSync(new TdApi.GetChatHistory(823105613L, 0, 0, 10, false), TdApi.Messages.class);
            TdApi.Messages messages = telegramClient.sendSync(new TdApi.GetChatHistory(823105613L, 0, 0, 10, false), TdApi.Messages.class);*/
            //int yesterday = (int) (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
            while (new Object().equals(null)) {
                List<Long> channelIds = List.of(6322607912L);
                Random random = new Random();
                Long fromChannelId = channelIds.get(random.nextInt(channelIds.size()));
                telegramClient.sendSync(new TdApi.GetChatHistory(fromChannelId, 0, 0, 10, false), TdApi.Messages.class);
                TdApi.Messages messages = telegramClient.sendSync(new TdApi.GetChatHistory(fromChannelId, 0, 0, 10, false), TdApi.Messages.class);
                List<TdApi.InputMessageContent> albumMessagesContent = new ArrayList<>();
                TdApi.InputMessageContent inputMessageContent;
                long albumId = 0;
                List<TdApi.Message> reversedMessages = Arrays.asList(messages.messages);
                Collections.reverse(reversedMessages);
                for (TdApi.Message message : reversedMessages) {
                    //if (message.date < yesterday || message.replyMarkup != null) {
                    //    continue;
                    //}
                    inputMessageContent = null;
                    if (message.content instanceof TdApi.MessageText messageText) {
                        TdApi.FormattedText text = messageText.text;
                        TdApiUtils.removeAllLinks(text);
                        inputMessageContent = new TdApi.InputMessageText(
                            text, false, true
                        );
                    }
                    if (message.content instanceof TdApi.MessageVideo messageVideo) {
                        TdApi.FormattedText caption = messageVideo.caption;
                        TdApiUtils.removeAllLinks(caption);
                        inputMessageContent = new TdApi.InputMessageVideo(
                            new TdApi.InputFileRemote(messageVideo.video.video.remote.id),
                            new TdApi.InputThumbnail(
                                new TdApi.InputFileRemote(messageVideo.video.thumbnail.file.remote.id),
                                messageVideo.video.thumbnail.width,
                                messageVideo.video.thumbnail.height
                            ),
                            null,
                            messageVideo.video.duration,
                            messageVideo.video.width,
                            messageVideo.video.height,
                            messageVideo.video.supportsStreaming,
                            caption,
                            null,
                            messageVideo.hasSpoiler
                        );
                    }
                    if (message.content instanceof TdApi.MessagePhoto messagePhoto) {
                        TdApi.FormattedText caption = messagePhoto.caption;
                        TdApiUtils.removeAllLinks(caption);
                        inputMessageContent = new TdApi.InputMessagePhoto(
                            new TdApi.InputFileRemote(messagePhoto.photo.sizes[0].photo.remote.id),
                            null,
                            null,
                            messagePhoto.photo.sizes[0].width,
                            messagePhoto.photo.sizes[0].height,
                            caption,
                            null,
                            messagePhoto.hasSpoiler
                        );
                    }
                    if (inputMessageContent != null) {
                        /*if (message.mediaAlbumId > 0 && (albumId == 0 || message.mediaAlbumId == albumId)) {
                            albumMessagesContent.add(inputMessageContent);
                            albumId = message.mediaAlbumId;
                        } else {
                            if (!albumMessagesContent.isEmpty()) {
                                if (random.nextBoolean()) {
                                    albumMessagesContent.clear();
                                    continue;
                                }
                                telegramClient.sendSync(
                                    new TdApi.SendMessageAlbum(
                                        -1001792690419L, 0, null, null,
                                        albumMessagesContent.toArray(new TdApi.InputMessageContent[0]),
                                        false
                                    ),
                                    TdApi.Messages.class
                                );
                                return;
                            }
                            if (message.mediaAlbumId > 0) {
                                albumMessagesContent.add(inputMessageContent);
                            } else {
                                if (random.nextBoolean()) {
                                    continue;
                                }
                                telegramClient.sendSync(
                                    new TdApi.SendMessage(
                                        -1001792690419L, 0, null, null, null,
                                        inputMessageContent
                                    ),
                                    TdApi.Message.class
                                );
                                return;
                            }
                        }*/
                    }
                }
            }
        };
    }

    public void loadChatsHandler(TdApi.Object object) {
        // https://core.telegram.org/tdlib/docs/classtd_1_1td__api_1_1load_chats.html
        // Returns a 404 error if all chats have been loaded.
        if (object instanceof TdApi.Ok) {
            TdApi.LoadChats loadChatsQuery = new TdApi.LoadChats(new TdApi.ChatListMain(), 100);
            telegramClient.sendWithCallback(loadChatsQuery, this::loadChatsHandler);
        } else {
            log.info("Chats loaded.");
        }
    }
}
