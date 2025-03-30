package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public User() {}

    public User(String email, String password,String fullName,  Role role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public User(String email, String password,String fullName,  Role role , String branch) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.branch = branch;
    }
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.fullName = "unknown";
        this.role = role;
    }

    public int getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    /**
     * Enum defining different user roles in the system.
     * - BRANCH_MANAGER: Manages a specific branch.
     * - GENERAL_MANAGER: Oversees the entire network of branches, System administrator with full permissions.
     * - DIETITIAN: Responsible for dietary and nutrition guidance.
     * - SERVICE_EMPLOYEE: Works in customer service.
     * - REGULAR_EMPLOYEE: A standard employee with basic permissions.
     */
    public enum Role {
        BRANCH_MANAGER,      // Manages a single branch
        GENERAL_MANAGER,     // Manages the entire network
        DIETITIAN,           // Handles dietary/nutrition tasks
        SERVICE_EMPLOYEE,    // Works in customer service
        REGULAR_EMPLOYEE,    // Standard employee role
        CLIENT
    }
}
