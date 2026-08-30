package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Items item;

    @ManyToOne
    @JoinColumn(name = "from_location_id", nullable = false)
    private InventoryLocations fromLocation;

    @ManyToOne
    @JoinColumn(name = "to_location_id", nullable = false)
    private InventoryLocations toLocation;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    public InventoryTransactions() {
        // JPA
    }

    public InventoryTransactions(Items item, InventoryLocations fromLocation, InventoryLocations toLocation, Integer quantity) {
        this.item = item;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.quantity = quantity;
        this.transactionDate = LocalDateTime.now();
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Items getItem() {
        return item;
    }

    public void setItem(Items item) {
        this.item = item;
    }

    public InventoryLocations getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(InventoryLocations fromLocation) {
        this.fromLocation = fromLocation;
    }

    public InventoryLocations getToLocation() {
        return toLocation;
    }

    public void setToLocation(InventoryLocations toLocation) {
        this.toLocation = toLocation;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}