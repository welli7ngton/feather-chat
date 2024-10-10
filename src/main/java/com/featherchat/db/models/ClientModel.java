package com.featherchat.db.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.security.crypto.bcript.BCryptPasswordEncoder;

@Entity
public class ClientModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column(nullable = false)
  private String password;

  public Long getId() {
    return this.id;
  }

  public ClientModel() {
  }

  // public ClientModel(String nickname, String password) {
  // this.nickname = nickname;
  // setPassword(password);
  // }

  public void setNickname(String newNickname) {
    this.nickname = newNickname;
  }

  public String getNickname() {
    return this.nickname;
  }

  // public void setPassword(String rawPassword) {
  // BCryptPasswordEncoder passwordEnconder = new BCryptPasswordEncoder();
  // this.password = passwordEnconder.encode(rawPassword);
  // }

}
