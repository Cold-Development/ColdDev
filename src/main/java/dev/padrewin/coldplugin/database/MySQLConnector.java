package dev.padrewin.coldplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.padrewin.coldplugin.ColdPlugin;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class MySQLConnector implements DatabaseConnector {

    private final ColdPlugin coldPlugin;
    private final String hostname;
    private final int port;
    private final String database;
    private HikariDataSource hikari;
    private final AtomicInteger openConnections;
    private final Object lock;


    public MySQLConnector(ColdPlugin coldPlugin, String hostname, int port, String database, String username, String password, boolean useSSL, int poolSize) {
        this.coldPlugin = coldPlugin;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.openConnections = new AtomicInteger();
        this.lock = new Object();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
            }
        }

        try {
            this.hikari = new HikariDataSource(config);
        } catch (Exception ex) {
            this.coldPlugin.getLogger().severe("Failed to connect to the MySQL server. Please check your credentials and connection settings.");
            ex.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        if (this.hikari != null && !this.hikari.isClosed()) {
            this.hikari.close();
        }
    }

    @Override
    public void connect(ConnectionCallback callback) {
        this.openConnections.incrementAndGet();
        try (Connection connection = this.hikari.getConnection()) {
            callback.accept(connection);
        } catch (SQLException ex) {
            this.coldPlugin.getLogger().severe("An error occurred executing a MySQL query: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            int open = this.openConnections.decrementAndGet();
            synchronized (this.lock) {
                if (open == 0) {
                    this.lock.notify();
                }
            }
        }
    }

    @Override
    public void connect(ConnectionCallback callback, boolean useTransaction) {
        this.connect(callback);
    }

    @Override
    public Object getLock() {
        return this.lock;
    }

    @Override
    public boolean isFinished() {
        return this.openConnections.get() == 0;
    }

    @Override
    public void cleanup() {
        // Optional: Add any MySQL-specific cleanup logic here
    }

    @Override
    public String getDatabasePath() {
        return "jdbc:mysql://" + hostname + ":" + port + "/" + database;
    }
}
