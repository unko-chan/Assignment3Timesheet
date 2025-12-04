package ca.bcit.infosys.liangk.assignment3.dto;

import ca.bcit.infosys.liangk.assignment3.entity.TimesheetStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimesheetDTO {
    private Long id;
    private LocalDate weekStart;
    private TimesheetStatus status;
    private BigDecimal totalHours;
    private Long userId; // owner id
    private List<TimesheetEntryDTO> entries = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }

    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<TimesheetEntryDTO> getEntries() { return entries; }
    public void setEntries(List<TimesheetEntryDTO> entries) { this.entries = entries; }
}
