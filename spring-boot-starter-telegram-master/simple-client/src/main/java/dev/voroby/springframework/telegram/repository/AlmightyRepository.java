package dev.voroby.springframework.telegram.repository;

import dev.voroby.springframework.telegram.model.AlbumIds;
import dev.voroby.springframework.telegram.model.ChannelPool;
import dev.voroby.springframework.telegram.model.MessageIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlmightyRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public static final RowMapper<ChannelPool> CHANNEL_POOL = BeanPropertyRowMapper.newInstance(ChannelPool.class);
    public static final RowMapper<MessageIds> MESSAGE_IDS = BeanPropertyRowMapper.newInstance(MessageIds.class);
    public static final RowMapper<AlbumIds> ALBUM_IDS = BeanPropertyRowMapper.newInstance(AlbumIds.class);

    /*public void insertChannelPool(ChannelPool pool) {
        jdbcTemplate.update(
            """
            INSERT INTO channel_pool(
            client_id,
            schedule,
            client_channel_id,
            channel_ids,
            expired_date
            )
            VALUES(?, ?, ?, ?, ?)
            """,
            pool.getClientId(),
            pool.getSchedule(),
            pool.getClientChannelId(),
            pool.getChannelIds(),
            pool.getExpiredDate()
        );
    }*/

    public List<ChannelPool> getAllActiveChannelPools(String stealTime) {
        return jdbcTemplate.query(
            "SELECT * FROM channel_pool WHERE schedule LIKE ? AND expired_date > now()",
            CHANNEL_POOL,
            "%" + stealTime + "%"
        );
    }

    public void updateLastMessagesParentIds(Long channelId, String newValue) {
        jdbcTemplate.update(
            "UPDATE channel_pool SET last_messages_parent_ids_list = ? WHERE channel_id = ?",
            newValue, channelId
        );
    }

    public MessageIds findContainingMessageId(String messageId) {
        return jdbcTemplate.query(
            "SELECT * FROM message_ids WHERE ids LIKE ?",
            MESSAGE_IDS,
            "%" + messageId + "%"
        ).stream().findFirst().orElse(null);
    }

    public void updateMessageIds(String oldValue, String newValue) {
        jdbcTemplate.update(
            "UPDATE message_ids SET ids = ? WHERE ids = ?",
            newValue, oldValue
        );
    }

    public void addMessageIds(String value) {
        jdbcTemplate.update(
            "INSERT INTO message_ids (ids) VALUES (?)",
            value
        );
    }

    public void updateLastAlbumsParentIds(Long channelId, String newValue) {
        jdbcTemplate.update(
            "UPDATE channel_pool SET last_albums_parent_ids_list = ? WHERE channel_id = ?",
            newValue, channelId
        );
    }

    public AlbumIds findContainingAlbumId(String albumId) {
        return jdbcTemplate.query(
            "SELECT * FROM album_ids WHERE ids LIKE ?",
            ALBUM_IDS,
            "%" + albumId + "%"
        ).stream().findFirst().orElse(null);
    }

    public void updateAlbumIds(String oldValue, String newValue) {
        jdbcTemplate.update(
            "UPDATE album_ids SET ids = ? WHERE ids = ?",
            newValue, oldValue
        );
    }

    public void addAlbumIds(String value) {
        jdbcTemplate.update(
            "INSERT INTO album_ids (ids) VALUES (?)",
            value
        );
    }
}
