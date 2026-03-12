package org.example.webapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.entity.Role_Permission.Role;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="user")
@SQLDelete(sql = "UPDATE user SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class User extends Base  {
    private String username;
    private String password;
    private String phone;
    private Date birthday;
    private double baseSalary=5000000.0;
    @ManyToMany(cascade = {
            CascadeType.MERGE
    })
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();
}
