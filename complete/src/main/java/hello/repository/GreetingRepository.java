package hello.repository;

import hello.entity.User;
import oracle.jdbc.OracleTypes;
import org.apache.commons.codec.Charsets;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<String> getPidListSP() {
        Map<String, Object> map = jdbcTemplate.call(conn -> {
            String sql = "{CALL DEV.QRY_PID_01(?)}";
            CallableStatement prepareCall = conn.prepareCall(sql);
            prepareCall.registerOutParameter(1, Types.VARCHAR);
            return prepareCall;
        }, SqlParameter.sqlTypesToAnonymousParameterList(OracleTypes.VARCHAR));
        System.out.println(map);
//        return jdbcTemplate.query("CALL DEV.QRY_PID_01(?)",
//                (rs, rowNum) -> new String(Base64.getDecoder().decode(rs.getString("CBI_CLT_PID")), Charsets.UTF_8)
//        );
        return null;
    }
}
