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

    public void setChannelIdsList(String channelIds) {
        this.channelIds = Arrays.stream(channelIds.split(";"))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    public String getChannelIdsList() {
        return Joiner.on(";").join(channelIds);
    }
}
