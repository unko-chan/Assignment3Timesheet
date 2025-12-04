package ca.bcit.infosys.liangk.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TimesheetStatus status = TimesheetStatus.OPEN;

    @Column(name = "total_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.totalHours == null) this.totalHours = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void recalcTotalHours() {
        BigDecimal sum = BigDecimal.ZERO;
        if (entries != null) {
            for (TimesheetEntry e : entries) {
                if (e != null && e.getHours() != null) {
                    sum = sum.add(e.getHours());
                }
            }
        }
        this.totalHours = sum;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TimesheetEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(TimesheetEntry entry) {
        if (entry == null) return;
        entry.setTimesheet(this);
        this.entries.add(entry);
        recalcTotalHours();
    }

    public void removeEntry(TimesheetEntry entry) {
        if (entry == null) return;
        this.entries.remove(entry);
        entry.setTimesheet(null);
        recalcTotalHours();
    }
}
