package dev.voroby.springframework.telegram.tms.jobs;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.model.ChannelPool;
import dev.voroby.springframework.telegram.repository.ClientRepository;
import dev.voroby.springframework.telegram.tms.SchedulerJob;
import dev.voroby.springframework.telegram.utils.TdApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class StealContentJob extends SchedulerJob {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private TelegramClient telegramClient;

    @Override
    public void call() {
        Thread.currentThread().setName("StealContentJob");
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        for (ChannelPool pool : clientRepository.getAllActiveChannelPools(currentTime)) {
            stealPost(pool);
        }
    }

    private void stealPost(ChannelPool pool) {
        int yesterday = (int) (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        while (true) {
            List<Long> channelIds = pool.getChannelIds();
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
                if (message.date < yesterday || message.replyMarkup != null) {
                    continue;
                }
                inputMessageContent = null;
                if (message.content instanceof TdApi.MessageText messageText) {
                    TdApi.FormattedText text = messageText.text;
                    TdApiUtils.removeAllLinks(text);
                    TdApiUtils.addLink(text, pool.getUrlTitle(), pool.getUrlChannel());
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
                    TdApiUtils.addLink(caption, pool.getUrlTitle(), pool.getUrlChannel());
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
                    if (message.mediaAlbumId > 0 && (albumId == 0 || message.mediaAlbumId == albumId)) {
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
                                    pool.getTgClientChannelId(), 0, null, null,
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
                                    pool.getTgClientChannelId(), 0, null, null, null,
                                    inputMessageContent
                                ),
                                TdApi.Message.class
                            );
                            return;
                        }
                    }
                }
            }
        }
    }
}
