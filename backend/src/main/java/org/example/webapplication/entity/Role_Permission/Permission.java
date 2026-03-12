package org.example.webapplication.entity.Role_Permission;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    @Id
    @Column(length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    private PermissionKey permission_key;

    @Enumerated(EnumType.STRING)
    private PermissionType permission_type;
}
