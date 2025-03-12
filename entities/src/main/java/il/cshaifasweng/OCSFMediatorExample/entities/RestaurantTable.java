package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "RestaurantTables")
public class RestaurantTable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int tableNumber;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean reserved = false;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public RestaurantTable() {}

    public RestaurantTable(int tableNumber, int capacity, Branch branch) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.branch = branch;
    }

    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
