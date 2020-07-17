package com.zy.integrate.junit;

import org.junit.Test;

import java.sql.*;

/**
 * @Author zhangyong
 * @Date 2020/7/13 22:24
 */
public class JunitTest {
    
    @Test
    public void JbdcMethod() throws ClassNotFoundException, SQLException {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet res = null;
        try {
           Class.forName("com.mysql.cj.jdbc.Driver");
           con = DriverManager.getConnection("jdbc:mysql://127.0.0.1/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8", "root", "12345678");
           pre = con.prepareStatement("select * from test where id = ?");
           pre.setInt(1,1);
           res = pre.executeQuery();
           while (res.next()){
               System.out.println(res.getString("name"));
           }
       }
       finally {
            // 关闭连接，释放资源
            if (res != null) {
                res.close();
            }
            if (pre != null) {
                pre.close();
            }
            if (con != null) {
                con.close();
            }
        }

    }
}
