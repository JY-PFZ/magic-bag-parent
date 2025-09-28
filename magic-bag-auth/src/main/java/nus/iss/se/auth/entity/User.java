package nus.iss.se.auth.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String phone;
    private String nickname;
    private String avatar;
    private Date createdAt;
    private Date updatedAt;
    private Integer status;
}
