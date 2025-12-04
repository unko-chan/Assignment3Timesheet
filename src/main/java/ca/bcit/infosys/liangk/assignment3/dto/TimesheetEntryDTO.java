package ca.bcit.infosys.liangk.assignment3.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetEntryDTO {
    private Long id;
    private LocalDate entryDate;
    private String projectCode;
    private String taskCode;
    private BigDecimal hours;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
