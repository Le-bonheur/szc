package com.ssc.szc.repository;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.jdbc.OracleTypes;
import org.apache.commons.codec.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Lebonheur
 */
@Component
public class GreetingRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public GreetingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void transactionTest(int i) {
        jdbcTemplate.update("INSERT INTO DEV.BLK_FLG_PID_T values(?)", "2李哥" + i);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void insert(String nickname) {
        jdbcTemplate.update("INSERT INTO DEV.BLK_FLG_PID_T values(?)", nickname);
    }

    public List<String> getPidList() {
        return jdbcTemplate.query("SELECT * FROM DEV.BLK_FLG_PID_T",
                (rs, rowNum) -> new String(Base64.getDecoder().decode(rs.getString("CBI_CLT_PID")), Charsets.UTF_8)
        );
    }

    public JSONArray getPidListSP() {
        Map<String, Object> map = jdbcTemplate.call(conn -> {
            String sql = "{CALL DEV.QRY_PID_01(?)}";
            CallableStatement prepareCall = conn.prepareCall(sql);
            prepareCall.registerOutParameter(1, OracleTypes.CURSOR);
            return prepareCall;
        }, Collections.singletonList(new SqlOutParameter("pid_list", OracleTypes.CURSOR)));

        System.out.println(map);

        JSONArray jsonArray = JSONArray.fromObject(map.get("pid_list"));

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));

            String pid = jsonObject.getString("PID");
            //Base64解码
            jsonObject.put("PID", new String(Base64.getDecoder().decode(pid), Charsets.UTF_8));

            jsonArray.set(i, jsonObject);
        }
        System.out.println(jsonArray);

        return jsonArray;
    }
}
