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
    private Long clientId;
    private String schedule;
    private Long clientChannelId;
    private List<Long> channelIdsList;
    private Date expiredDate;

    public void setChannelIds(String channelIds) {
        this.channelIdsList = Arrays.stream(channelIds.split(";"))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    public String getChannelIds() {
        return Joiner.on(";").join(channelIdsList);
    }
}
