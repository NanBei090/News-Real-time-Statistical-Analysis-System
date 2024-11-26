package com.spark.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author nanbei
 * @since 2024-11-24
 */
public class WeblogService {
    private static final String URL = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123580asd";

    private static final Logger logger = LoggerFactory.getLogger(WeblogService.class);

    /**
     * 按话题分组，取统计数据
     *
     * @return 话题名称和统计数据的 Map
     */
    public Map<String, Object> queryWeblog() {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        Map<String, Object> retMap = new HashMap<>();
        List<String> titleNames = new ArrayList<>();
        List<Integer> titleCounts = new ArrayList<>();

        try {
            // 加载 MySQL 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 连接数据库
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            // 执行 SQL 查询
            String querySql = "SELECT titleName, count FROM title_counts ORDER BY count DESC LIMIT 20";
            pst = conn.prepareStatement(querySql);

            rs = pst.executeQuery();
            while (rs.next()) {
                titleNames.add(rs.getString("titleName"));
                titleCounts.add(rs.getInt("count"));
            }

            retMap.put("titleName", titleNames);
            retMap.put("titleCount", titleCounts);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error executing queryWeblog", e);
        } finally {
            closeResources(rs, pst, conn);
        }

        return retMap;
    }

    /**
     * 获取话题总数
     *
     * @return 话题总数的数组
     */
    public String[] titleCount() {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        String[] titleSums = new String[1];

        try {
            // 加载 MySQL 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 连接数据库
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            // 执行 SQL 查询
            String querySql = "SELECT COUNT(1) AS titleSum FROM title_counts";
            pst = conn.prepareStatement(querySql);

            rs = pst.executeQuery();
            if (rs.next()) {
                titleSums[0] = rs.getString("titleSum");
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error executing titleCount", e);
        } finally {
            closeResources(rs, pst, conn);
        }

        return titleSums;
    }

    /**
     * 关闭数据库资源
     */
    private void closeResources(ResultSet rs, PreparedStatement pst, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing database resources", e);
        }
    }
}
