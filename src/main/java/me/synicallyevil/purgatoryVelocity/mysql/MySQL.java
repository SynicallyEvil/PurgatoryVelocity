package me.synicallyevil.purgatoryVelocity.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQL {

    private Connection connection;
    private final String ip;
    private final String username;
    private final String password;
    private final String db;

    public MySQL(String ip, String userName, String password, String db) {
        enableConnection(ip, userName, password, db);
        createUsers();

        this.ip = ip;
        this.username = userName;
        this.password = password;
        this.db = db;
    }

    public boolean isConnected(){
        try{
            return (connection != null) && !(connection.isClosed());
        }catch(Exception ignored){}

        return false;
    }

    public void enableConnection(String ip, String userName, String password, String db){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + "/" + db + "?user=" + userName + "&password=" + password + "&autoReconnect=true");
            //connection = DriverManager.getConnection("jdbc:mysql://" + this.ip + "/" + this.db + "?autoReconnect=true", this.username, this.password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableConnection(){
        try {
            if((connection != null) && !(connection.isClosed()))
                connection.close();

        }catch(Exception ignored){}
    }

    public void createUsers() {
        if(!isConnected()){
            System.out.println("It appears the connection was disrupted. Attempting to reconnect..");
            enableConnection(ip, username, password, db);
        }

        try {
            String table = "CREATE TABLE IF NOT EXISTS users(id VARCHAR(64), uuid VARCHAR(64), reason VARCHAR(64), staff VARCHAR(16), time BIGINT);";
            PreparedStatement statement = connection.prepareStatement(table);
            statement.executeUpdate();
            statement.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public MySQLUser getMYSQLUser(String uuid){
        if(!isConnected()){
            System.out.println("It appears the connection was disrupted. Attempting to reconnect..");
            enableConnection(ip, username, password, db);
        }

        try{
            String sql = "SELECT * FROM users WHERE uuid='"+ uuid + "'";
            java.sql.PreparedStatement myPreparedStatement = connection.prepareStatement(sql);
            ResultSet result = myPreparedStatement.executeQuery();

            boolean isBanned = false;
            String id = "000000";
            String reason = "N/A";
            String staff = "N/A";
            long time = 999;
            while(result.next()){
                isBanned = true;
                id = result.getString("id");
                reason = result.getString("reason");
                staff = result.getString("staff");
                time = result.getLong("time");
            }

            result.close();
            myPreparedStatement.close();
            return new MySQLUser(isBanned, uuid, id, reason, staff, time);
        }catch(SQLException ex){
            ex.printStackTrace();
        }

        return new MySQLUser(false, uuid, "000000", "N/A", "N/A", 0);
    }

    public List<MySQLUser> getAllUsers(){
        List<MySQLUser> users = new ArrayList<>();

        try{
            String sql = "SELECT * FROM users WHERE 1";
            java.sql.PreparedStatement myPreparedStatement = connection.prepareStatement(sql);
            ResultSet result = myPreparedStatement.executeQuery();

            while(result.next()){
                boolean isBanned = true;
                String id = result.getString("id");
                String uuid = result.getString("uuid");
                String reason = result.getString("reason");
                String staff = result.getString("staff");
                Long time = result.getLong("time");

                users.add(new MySQLUser(isBanned, uuid, id, reason, staff, time));
            }

            result.close();
            myPreparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    public void banPlayer(String uuid, String reason, String staff, String id, long time) {
        if(!isConnected()){
            System.out.println("It appears the connection was disrupted. Attempting to reconnect..");
            enableConnection(ip, username, password, db);
        }

        try {
            PreparedStatement statement = connection.prepareStatement("insert into users (id, uuid, reason, staff, time)\nvalues ('" + id + "', '" + uuid + "', '" + reason + "', '" + staff + "', '" + time + "');");
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unbanPlayer(String uuid) {
        if(!isConnected()){
            System.out.println("It appears the connection was disrupted. Attempting to reconnect..");
            enableConnection(ip, username, password, db);
        }

        try {
            PreparedStatement statement = connection.prepareStatement("delete from users where uuid='" + uuid +"';");
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkColumn(String column, String type){
        try{
            PreparedStatement statement = connection.prepareStatement("ALTER TABLE `blacklists`.`users` CHANGE COLUMN `` `" + column + "` " + type + " NULL;");
            statement.executeQuery();
            statement.close();
        }catch (SQLException ex){
            ex.getMessage();
        }
    }
}