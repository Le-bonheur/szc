package com.ssc.szc.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ssc.szc.sql.SqlSet;

import java.util.List;

/**
 * @author Lebonheur
 */
@Component
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 更新SESSION_KEY
     *
     * @param code 登录凭证
     * @param sessionKey 会话密钥
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateSessionKey(String code, String sessionKey) {
        int updateRows = jdbcTemplate.update(SqlSet.UPDATE_SESSION_KEY, code, sessionKey);
        if(updateRows != 1) {
            throw new RuntimeException("sessionKey没有行被更新");
        }
    }

    /**
     * 通过openId查询返回session_key
     *
     * @param openId openId
     * @return session_key
     */
    public String getKeyByOpenId(String openId) {
        List<String> result = jdbcTemplate.query(SqlSet.QUERY_SESSION_KEY_BY_ID,
                (rs, rowNum) -> rs.getString("SESSION_KEY"), openId);

        if(result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * 插入黑名单
     *
     * @param openId openId
     * @param type 类型
     */
    public void insertBlacklist(String openId, String type) {
        jdbcTemplate.update(SqlSet.INSERT_BLACKLIST, openId, type);
    }

}
