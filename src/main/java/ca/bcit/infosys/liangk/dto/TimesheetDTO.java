package ca.bcit.infosys.liangk.dto;

import ca.bcit.infosys.liangk.entity.TimesheetStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object representing a timesheet and its entries.
 */
public class TimesheetDTO {
    private Long id;
    private String weekStart; // ISO yyyy-MM-dd
    private TimesheetStatus status;
    private BigDecimal totalHours;
    private List<TimesheetEntryDTO> entries = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(String weekStart) {
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

    public List<TimesheetEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntryDTO> entries) {
        this.entries = entries;
    }
}
