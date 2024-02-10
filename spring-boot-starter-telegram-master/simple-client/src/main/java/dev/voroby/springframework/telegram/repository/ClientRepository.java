package dev.voroby.springframework.telegram.repository;

import dev.voroby.springframework.telegram.model.ChannelPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClientRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public static final RowMapper<ChannelPool> CHANNEL_POOL = BeanPropertyRowMapper.newInstance(ChannelPool.class);

    public void insertChannelPool(ChannelPool pool) {
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
    }

    public List<ChannelPool> getAllActiveChannelPools(String stealTime) {
        return jdbcTemplate.query(
            "SELECT * FROM channel_pool WHERE schedule LIKE ?",
            CHANNEL_POOL,
            "%" + stealTime + "%"
        );
    }
}
