package me.synicallyevil.purgatoryVelocity.mysql;

public class MySQLUser {

    private Boolean banned;
    private String uuid;
    private String id;
    private String reason;
    private String staff;
    private long time;

    public MySQLUser(boolean banned, String uuid, String id, String reason, String staff, long time){
        this.banned = banned;
        this.uuid = uuid;
        this.id = id;
        this.reason = reason;
        this.staff = staff;
        this.time = time;
    }

    public Boolean isBanned() {
        return banned;
    }

    public String getUUID() {
        return uuid;
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public String getStaff() {
        return staff;
    }

    public long getTime() {
        return time;
    }
}
