package dev.voroby.springframework.telegram.model;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ChannelPool {
    private Long channelId;
    private Long tgClientChannelId;
    private Long tgClientId;
    private String schedule;
    private String clientChannelName;
    private List<Long> channelIds;
    private Date expiredDate;
    private String urlTitle;
    private String urlChannel;
    private List<String> lastMessagesParentIds;
    private List<String> lastAlbumsParentIds;

    public void setChannelIdsList(String channelIds) {
        this.channelIds = Arrays.stream(channelIds.split(";"))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    public String getChannelIdsList() {
        return Joiner.on(";").join(this.channelIds);
    }

    public void setLastMessagesParentIdsList(String channelIds) {
        this.lastMessagesParentIds = Arrays.stream(channelIds.split(";"))
            .collect(Collectors.toList());
    }

    public String getLastMessagesParentIdsList() {
        if (this.lastMessagesParentIds == null) return "";
        int size = this.lastMessagesParentIds.size();
        if (size > 200) this.lastMessagesParentIds = this.lastMessagesParentIds.subList(size - 201, size - 1);
        return Joiner.on(";").join(this.lastMessagesParentIds);
    }

    public boolean isMessageAlreadyPosted(MessageIds messageIds) {
        return this.lastMessagesParentIds != null &&
            this.lastMessagesParentIds.stream().anyMatch(id -> messageIds.getIds().contains(id));
    }

    public void setLastAlbumsParentIdsList(String channelIds) {
        this.lastAlbumsParentIds = Arrays.stream(channelIds.split(";"))
            .collect(Collectors.toList());
    }

    public String getLastAlbumsParentIdsList() {
        if (this.lastAlbumsParentIds == null) return "";
        int size = this.lastAlbumsParentIds.size();
        if (size > 200) this.lastAlbumsParentIds = this.lastAlbumsParentIds.subList(size - 201, size - 1);
        return Joiner.on(";").join(this.lastAlbumsParentIds);
    }

    public boolean isAlbumAlreadyPosted(AlbumIds albumIds) {
        return this.lastAlbumsParentIds != null &&
            this.lastAlbumsParentIds.stream().anyMatch(id -> albumIds.getIds().contains(id));
    }
}
