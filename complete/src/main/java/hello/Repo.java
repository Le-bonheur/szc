package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class Repo {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public Repo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    TransactionTemplate template = new TransactionTemplate();

//    @Transactional(rollbackFor = RuntimeException.class)
    public void transactionTest(int i) {
        jdbcTemplate.update("INSERT INTO DEV.BLK_FLG_PID_T values(?)", "2李哥" + i);
    }

    private Connection conn = null;


    // 获得连接对象
    private synchronized Connection getConn() {
        if (conn == null) {
            try {
                String driver = "oracle.jdbc.driver.OracleDriver";
                Class.forName(driver);
                String url = "jdbc:oracle:thin:@193.112.211.149:1521:orcl";
                String username = "szc";
                String password = "Ss20180601";
                conn = DriverManager.getConnection(url, username, password);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    //执行查询语句
    public List<String> query(String sql, boolean isSelect) throws SQLException {
        PreparedStatement pstmt;
        List<String> pids = new ArrayList<>();
        try {
            pstmt = getConn().prepareStatement(sql);
            //建立一个结果集，用来保存查询出来的结果
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pids.add(rs.getString(1));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pids;
    }

    public void query(String sql) throws SQLException {
        PreparedStatement pstmt;
        pstmt = getConn().prepareStatement(sql);
        pstmt.execute();
        pstmt.close();
    }


    //关闭连接
    public void close() {
        try {
            getConn().close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
