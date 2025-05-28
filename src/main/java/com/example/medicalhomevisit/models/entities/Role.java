package com.example.medicalhomevisit.models.entities;

import com.example.medicalhomevisit.models.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Role extends BaseEntity {

    private UserRole name;
    private Set<UserEntity> users;

    public Role() {
        users = new HashSet<>();
    }

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    public UserRole getName() {
        return name;
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }
}
