package dev.voroby.springframework.telegram.tms.jobs;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.interfaces.HasText;
import dev.voroby.springframework.telegram.model.AlbumIds;
import dev.voroby.springframework.telegram.model.ChannelPool;
import dev.voroby.springframework.telegram.model.MessageIds;
import dev.voroby.springframework.telegram.repository.AlmightyRepository;
import dev.voroby.springframework.telegram.tms.SchedulerJob;
import dev.voroby.springframework.telegram.utils.TdApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StealContentJob extends SchedulerJob {
    private static final int MESSAGES_LIMIT = 20;

    @Autowired
    private AlmightyRepository almightyRepository;
    @Autowired
    private TelegramClient telegramClient;

    @Override
    public void call() {
        Thread.currentThread().setName("StealContentJob");
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        for (ChannelPool pool : almightyRepository.getAllActiveChannelPools(currentTime)) {
            tryStealPost(pool);
        }
    }

    private void tryStealPost(ChannelPool pool) {
        int yesterday = (int) (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        int tryCount = 0;
        while (tryCount < 10) {
            try {
                tryCount++;
                if (stealPost(pool, yesterday)) return;
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        log.error("Can't steal post for channel_pool with channelId = '%s'".formatted(pool.getChannelId()));
    }

    private boolean stealPost(ChannelPool pool, int yesterday) {
        Random random = new Random();
        for (TdApi.Message message : getRandomMessages(pool, random)) {
            if (message.date < yesterday || message.replyMarkup != null) continue;
            if (processMessage(pool, message, random)) return true;
        }
        return false;
    }

    private List<TdApi.Message> getRandomMessages(ChannelPool pool, Random random) {
        List<Long> channelIds = pool.getChannelIds();
        Long fromChannelId = channelIds.get(random.nextInt(channelIds.size()));
        return getMessages(fromChannelId, MESSAGES_LIMIT);
    }

    private List<TdApi.Message> getAlbum(TdApi.Message message) {
        return getMessages(message.chatId, MESSAGES_LIMIT + 30).stream()
            .filter(e -> e.mediaAlbumId == message.mediaAlbumId)
            .collect(Collectors.toList());
    }

    private List<TdApi.Message> getMessages(Long fromChannelId, int messagesCount) {
        telegramClient.sendSync(new TdApi.GetChatHistory(fromChannelId, 0, 0, messagesCount, false), TdApi.Messages.class);
        TdApi.Messages messages = telegramClient.sendSync(new TdApi.GetChatHistory(fromChannelId, 0, 0, messagesCount, false), TdApi.Messages.class);
        List<TdApi.Message> reversedMessages = Arrays.asList(messages.messages);
        Collections.reverse(reversedMessages);
        return reversedMessages;
    }

    private TdApi.InputMessageContent getMessageContent(TdApi.Message message, ChannelPool pool) {
        if (message.content instanceof TdApi.MessageText messageText) {
            return getTextMessageContent(messageText, pool);
        }
        if (message.content instanceof TdApi.MessageVideo messageVideo) {
            return getVideoMessageContent(messageVideo, pool);
        }
        if (message.content instanceof TdApi.MessagePhoto messagePhoto) {
            return getPhotoMessageContent(messagePhoto, pool);
        }
        if (message.content instanceof TdApi.MessageAudio messageAudio) {
            return getAudioMessageContent(messageAudio, pool);
        }
        if (message.content instanceof TdApi.MessageDocument messageDocument) {
            return getDocumentMessageContent(messageDocument, pool);
        }
        return null;
    }

    private TdApi.InputMessageContent getTextMessageContent(TdApi.MessageText message, ChannelPool pool) {
        return new TdApi.InputMessageText(
            TdApiUtils.processText(message, pool),
            false,
            true
        );
    }

    private TdApi.InputMessageContent getVideoMessageContent(TdApi.MessageVideo message, ChannelPool pool) {
        var video = new TdApi.InputFileRemote(message.video.video.remote.id);
        var thumbnail = message.video.thumbnail;

        return new TdApi.InputMessageVideo(
            video,
            thumbnail == null ? null : new TdApi.InputThumbnail(video, thumbnail.width, thumbnail.height),
            null,
            message.video.duration,
            message.video.width,
            message.video.height,
            message.video.supportsStreaming,
            TdApiUtils.processText(message, pool),
            null,
            message.hasSpoiler
        );
    }

    private TdApi.InputMessageContent getPhotoMessageContent(TdApi.MessagePhoto message, ChannelPool pool) {
        return new TdApi.InputMessagePhoto(
            new TdApi.InputFileRemote(message.photo.sizes[0].photo.remote.id),
            null,
            null,
            message.photo.sizes[0].width,
            message.photo.sizes[0].height,
            TdApiUtils.processText(message, pool),
            null,
            message.hasSpoiler
        );
    }

    private TdApi.InputMessageContent getAudioMessageContent(TdApi.MessageAudio message, ChannelPool pool) {
        var audio = new TdApi.InputFileRemote(message.audio.audio.remote.id);
        var thumbnail = message.audio.albumCoverThumbnail;

        return new TdApi.InputMessageAudio(
            audio,
            thumbnail == null ? null : new TdApi.InputThumbnail(audio, thumbnail.width, thumbnail.height),
            message.audio.duration,
            message.audio.title,
            message.audio.performer,
            TdApiUtils.processText(message, pool)
        );
    }

    private TdApi.InputMessageContent getDocumentMessageContent(TdApi.MessageDocument message, ChannelPool pool) {
        var file = new TdApi.InputFileRemote(message.document.document.remote.id);
        var thumbnail = message.document.thumbnail;

        return new TdApi.InputMessageDocument(
            file,
            thumbnail == null ? null : new TdApi.InputThumbnail(file, thumbnail.width, thumbnail.height),
            false,
            TdApiUtils.processText(message, pool)
        );
    }

    private boolean processMessage(ChannelPool pool, TdApi.Message message, Random random) {
        return message.mediaAlbumId > 0 ? processAlbum(pool, message, random) : processSingleMessage(pool, message);
    }

    private boolean processAlbum(ChannelPool pool, TdApi.Message message, Random random) {
        if (random.nextBoolean()) return false;

        String stealingAlbumId = message.chatId + ":" + message.mediaAlbumId;
        AlbumIds albumIds = almightyRepository.findContainingAlbumId(stealingAlbumId);
        if (pool.isAlbumAlreadyPosted(Objects.requireNonNullElse(albumIds, new AlbumIds(stealingAlbumId)))) {
            return false;
        }

        var messages = getAlbum(message);
        var albumMessagesContent = messages.stream().map(e -> getMessageContent(e, pool)).filter(Objects::nonNull).toList();
        if (albumMessagesContent.isEmpty()) return false;

        if (albumMessagesContent.stream()
            .noneMatch(e -> e instanceof HasText hasText && hasText.getText() != null &&
                StringUtils.isNotEmpty(hasText.getText().text))) {
            TdApi.FormattedText text = new TdApi.FormattedText();
            TdApiUtils.addLink(text, pool.getUrlTitle(), pool.getUrlChannel());
            ((HasText) albumMessagesContent.get(0)).setText(text);
        }

        TdApi.Messages posted = telegramClient.sendSync(
            new TdApi.SendMessageAlbum(
                pool.getTgClientChannelId(), 0, null, null,
                albumMessagesContent.toArray(new TdApi.InputMessageContent[0]),
                false
            ),
            TdApi.Messages.class
        );

        String postedId = posted.messages[0].chatId + ":" + posted.messages[0].mediaAlbumId;
        if (albumIds == null) {
            almightyRepository.addAlbumIds(stealingAlbumId + ";" + postedId);
        } else {
            almightyRepository.updateAlbumIds(albumIds.getIds(), albumIds.getIds() + ";" + postedId);
        }
        almightyRepository.updateLastAlbumsParentIds(
            pool.getChannelId(),
            pool.getLastAlbumsParentIdsList() + ";" + stealingAlbumId
        );

        return true;
    }

    private boolean processSingleMessage(ChannelPool pool, TdApi.Message message) {
        // У альбома больше шансов быть спизженным чем у обычного поста,
        // поэтому рандомно прерываем пиздинг только для альбомов, чтобы уравнять шансы.
        // По-хорошему бы вообще сделать зависимость между количеством элементов в альбоме
        // и вероятностью его пиздинга (для более равномерного распределения вероятностей).
        /*if (random.nextBoolean()) {
            return false;
        }*/

        var inputMessageContent = getMessageContent(message, pool);
        if (inputMessageContent == null) return false;

        String stealingMessageId = message.chatId + ":" + message.id;
        MessageIds messageIds = almightyRepository.findContainingMessageId(stealingMessageId);
        if (pool.isMessageAlreadyPosted(Objects.requireNonNullElse(messageIds, new MessageIds(stealingMessageId)))) {
            return false;
        }

        TdApi.Message posted = telegramClient.sendSync(
            new TdApi.SendMessage(
                pool.getTgClientChannelId(), 0, null, null, null,
                inputMessageContent
            ),
            TdApi.Message.class
        );

        String postedId = posted.chatId + ":" + posted.id;
        if (messageIds == null) {
            almightyRepository.addMessageIds(stealingMessageId + ";" + postedId);
        } else {
            almightyRepository.updateMessageIds(messageIds.getIds(), messageIds.getIds() + ";" + postedId);
        }
        almightyRepository.updateLastMessagesParentIds(
            pool.getChannelId(),
            pool.getLastMessagesParentIdsList() + ";" + stealingMessageId
        );

        return true;
    }
}
