package com.example.demo.models;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


@Entity
@Table(name = "users")
public class User {
    public static final int MAX_WEBSITES_TO_CHECK = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(max = 255)
    private String name;

    @NotEmpty
    @Email
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    @NotEmpty
    @Size(max = 255)
    private String password;



    private int remainingLimit=MAX_WEBSITES_TO_CHECK ; // Default limit

    private int WebsiteRemainingLimit=MAX_WEBSITES_TO_CHECK ; 

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRemainingLimit() {
        return remainingLimit;
    }

    public void setRemainingLimit(int remainingLimit) {
        this.remainingLimit = remainingLimit;
    }


 
    public int getWebsiteRemainingLimit() {
        return WebsiteRemainingLimit;
    }

    public void setWebsiteRemainingLimit(int websiteRemainingLimit) {
        WebsiteRemainingLimit = websiteRemainingLimit;
    }


   @PreUpdate
    public void setLastUpdate() {
        this.lastUpdate = LocalDateTime.now();
    } 

  
//    @PreUpdate
//     public void preUpdate() {
//         LocalDateTime now = LocalDateTime.now();
//         if (lastUpdate != null && Duration.between(lastUpdate, now).toDays() >= 1) {
//             // One day has passed, reset remaining limit
//             remainingLimit = MAX_WEBSITES_TO_CHECK;
//         }
//         lastUpdate = now; // Update lastUpdate field to current time
//     }
 }

