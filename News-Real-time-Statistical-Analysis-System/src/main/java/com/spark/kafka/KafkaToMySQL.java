package com.spark.kafka;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.Properties;

/**
 * @author nanbei
 * @since 2024/11/23
 */
public class KafkaToMySQL {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/test"; // 替换为你的 MySQL 数据库地址
    private static final String USERNAME = "root"; // MySQL 用户名
    private static final String PASSWORD = "123580asd"; // MySQL 密码

    // 配置数据库连接池
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10); // 连接池大小
        dataSource = new HikariDataSource(config);
    }

    public static void main(String[] args) {
        // 配置 Kafka 消费者
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop102:9092"); // 替换为你的 Kafka 地址
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "1"); // 消费者组 ID
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 订阅 Kafka 主题
        consumer.subscribe(Collections.singletonList("weblogs"));

        try {
            while (true) {
                // 从 Kafka 中拉取消息
                consumer.poll(1000).forEach(record -> {
                    String message = record.value();
                    System.out.println("Consumed message: " + message);  // 输出到终端

                    // 假设每条消息是逗号分隔的数据格式
                    String[] fields = message.split(",");
                    if (fields.length < 6) {
                        System.out.println("Invalid message format: " + message);
                        return;
                    }
                    String datetime = fields[0];
                    String userid = fields[1];
                    String searchname = fields[2];
                    String retorder = fields[3];
                    String cliorder = fields[4];
                    String cliurl = fields[5];

                    // 将数据保存到数据库
                    boolean isSaved = saveToDatabase(datetime, userid, searchname, retorder, cliorder, cliurl);
                    if (isSaved) {
                        System.out.println("Data successfully saved to database: " + message);  // 输出到终端
                    } else {
                        System.out.println("Failed to save data to database: " + message);  // 输出到终端
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    // 将数据插入到 MySQL 数据库
    private static boolean saveToDatabase(String datetime, String userid, String searchname, String retorder, String cliorder, String cliurl) {
        try (Connection connection = dataSource.getConnection()) {
            // 插入日志数据到 weblogs 表
            String insertWeblogSql = "INSERT INTO weblogs(datetime, userid, searchname, retorder, cliorder, cliurl) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE searchname = VALUES(searchname), " +
                    "retorder = VALUES(retorder), cliorder = VALUES(cliorder), cliurl = VALUES(cliurl)";
            try (PreparedStatement insertWeblogStmt = connection.prepareStatement(insertWeblogSql)) {
                insertWeblogStmt.setString(1, datetime);
                insertWeblogStmt.setString(2, userid);
                insertWeblogStmt.setString(3, searchname);
                insertWeblogStmt.setString(4, retorder);
                insertWeblogStmt.setString(5, cliorder);
                insertWeblogStmt.setString(6, cliurl);
                insertWeblogStmt.executeUpdate();
            }

            // 更新 title_counts 表，统计查询词的出现次数
            String updateTitleCountSql = "INSERT INTO title_counts(titleName, count) " +
                    "VALUES (?, 1) " +
                    "ON DUPLICATE KEY UPDATE count = count + 1";
            try (PreparedStatement updateTitleCountStmt = connection.prepareStatement(updateTitleCountSql)) {
                updateTitleCountStmt.setString(1, searchname);
                updateTitleCountStmt.executeUpdate();
            }

            return true; // 如果插入或更新成功，则返回 true
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 如果有异常发生，返回 false
        }
    }

}

