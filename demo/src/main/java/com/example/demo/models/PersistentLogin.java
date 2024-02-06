package com.example.demo.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "persistent_logins")
public class PersistentLogin {

    @Id
    private String series;
    private String username;
    private String token;
    private Date lastUsed;
    public String getSeries() {
        return series;
    }
    public void setSeries(String series) {
        this.series = series;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public Date getLastUsed() {
        return lastUsed;
    }
    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    // Getters and setters
}
