package com.bookstore.bike.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity @Table(name="users", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String fullName;
 @JsonIgnore @Column(nullable=false) private String email;
 @JsonIgnore @Column(nullable=false) private String password;
 @JsonIgnore private String phone; @JsonIgnore private String college; @JsonIgnore private String studentId; private String city;
 @Enumerated(EnumType.STRING) private Enums.Role role=Enums.Role.STUDENT;
 @Enumerated(EnumType.STRING) private Enums.VerificationStatus verificationStatus=Enums.VerificationStatus.PENDING;
 private boolean active=true;
 private Instant createdAt=Instant.now();
 public Long getId(){return id;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPassword(){return password;} public void setPassword(String v){password=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getCollege(){return college;} public void setCollege(String v){college=v;}
 public String getStudentId(){return studentId;} public void setStudentId(String v){studentId=v;} public String getCity(){return city;} public void setCity(String v){city=v;}
 public Enums.Role getRole(){return role;} public void setRole(Enums.Role v){role=v;} public Enums.VerificationStatus getVerificationStatus(){return verificationStatus;} public void setVerificationStatus(Enums.VerificationStatus v){verificationStatus=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
