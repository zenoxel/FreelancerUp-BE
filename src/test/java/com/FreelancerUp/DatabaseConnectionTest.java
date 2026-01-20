package com.FreelancerUp;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class DatabaseConnectionTest {

    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
        }
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull();
        assertThat(mongoClient).isNotNull();
        assertThat(redisTemplate).isNotNull();
    }

    @Test
    void testPostgreSQLConnection() {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue();

            var metaData = connection.getMetaData();
            System.out.println("✓ PostgreSQL Connected:");
            System.out.println("  - Database: " + metaData.getDatabaseProductName());
            System.out.println("  - Version: " + metaData.getDatabaseProductVersion());
            System.out.println("  - URL: " + metaData.getURL());
        } catch (Exception e) {
            throw new AssertionError("PostgreSQL connection failed", e);
        }
    }

    @Test
    void testMongoDBConnection() {
        try {
            MongoDatabase database = mongoClient.getDatabase("freelancerup_db");
            assertThat(database).isNotNull();

            database.runCommand(org.bson.Document.parse("{ping: 1}"));

            System.out.println("✓ MongoDB Connected:");
            System.out.println("  - Database: " + database.getName());
            System.out.println("  - Cluster description: " + mongoClient.getClusterDescription());
        } catch (Exception e) {
            throw new AssertionError("MongoDB connection failed", e);
        }
    }

    @Test
    void testRedisConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();

            redisTemplate.opsForValue().set("test:connection", "ok");
            String value = (String) redisTemplate.opsForValue().get("test:connection");
            assertThat(value).isEqualTo("ok");
            redisTemplate.delete("test:connection");

            System.out.println("✓ Redis Connected:");
            System.out.println("  - Ping successful");
            System.out.println("  - Read/Write test passed");
        } catch (Exception e) {
            throw new AssertionError("Redis connection failed", e);
        }
    }

    @Test
    void testAllDatabases() {
        System.out.println("\n═════════════════════════════════════════════════════");
        System.out.println("           DATABASE CONNECTION TEST");
        System.out.println("═════════════════════════════════════════════════════\n");

        testPostgreSQLConnection();
        testMongoDBConnection();
        testRedisConnection();

        System.out.println("\n✓ ALL DATABASE CONNECTIONS PASSED");
        System.out.println("═════════════════════════════════════════════════════\n");
    }
}
