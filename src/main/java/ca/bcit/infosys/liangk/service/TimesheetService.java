package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.TimesheetDAO;
import ca.bcit.infosys.liangk.dto.TimesheetDTO;
import ca.bcit.infosys.liangk.dto.TimesheetEntryDTO;
import ca.bcit.infosys.liangk.entity.Timesheet;
import ca.bcit.infosys.liangk.entity.TimesheetEntry;
import ca.bcit.infosys.liangk.entity.TimesheetStatus;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.exception.ForbiddenException;
import ca.bcit.infosys.liangk.exception.NotFoundException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Stateless
public class TimesheetService {

    @Inject
    private TimesheetDAO timesheetDAO;

    // List timesheets for the current user; optional filter by specific week
    public List<Timesheet> listTimesheets(User currentUser, Optional<LocalDate> weekStart) {
        requireUser(currentUser);
        if (weekStart != null && weekStart.isPresent()) {
            Timesheet ts = timesheetDAO.findByUserAndWeek(currentUser.getId(), weekStart.get());
            List<Timesheet> list = new ArrayList<>();
            if (ts != null) list.add(ts);
            return list;
        }
        return timesheetDAO.findByUser(currentUser.getId());
    }

    public Timesheet getTimesheet(User currentUser, long id) {
        requireUser(currentUser);
        Timesheet t = timesheetDAO.findById(id);
        if (t == null) throw new NotFoundException("Timesheet not found: id=" + id);
        if (!isOwnerOrAdmin(currentUser, t)) {
            throw new ForbiddenException("Not allowed to access this timesheet");
        }
        return t;
    }

    public Timesheet createTimesheet(User currentUser, TimesheetDTO dto) {
        requireUser(currentUser);
        if (dto == null) throw new ValidationException("Timesheet payload is required");
        LocalDate week = parseWeekStart(dto.getWeekStart());
        // Ensure not existing for same week
        if (timesheetDAO.findByUserAndWeek(currentUser.getId(), week) != null) {
            throw new ValidationException("Timesheet already exists for week start " + week);
        }
        Timesheet ts = new Timesheet();
        ts.setOwner(currentUser);
        ts.setWeekStart(week);
        ts.setStatus(dto.getStatus() == null ? TimesheetStatus.OPEN : dto.getStatus());
        ts.setEntries(new ArrayList<>());

        if (dto.getEntries() != null) {
            for (TimesheetEntryDTO e : dto.getEntries()) {
                ts.addEntry(buildEntryFromDTO(e, ts));
            }
        }
        ts.recalcTotalHours();
        return timesheetDAO.create(ts);
    }

    public Timesheet updateTimesheet(User currentUser, long id, TimesheetDTO dto) {
        requireUser(currentUser);
        if (dto == null) throw new ValidationException("Timesheet payload is required");
        Timesheet ts = getTimesheet(currentUser, id);

        // Only owner or admin can update; already enforced by getTimesheet
        if (dto.getWeekStart() != null) {
            LocalDate newWeek = parseWeekStart(dto.getWeekStart());
            if (!newWeek.equals(ts.getWeekStart())) {
                // Check uniqueness for new week
                Timesheet existing = timesheetDAO.findByUserAndWeek(ts.getOwner().getId(), newWeek);
                if (existing != null && !existing.getId().equals(ts.getId())) {
                    throw new ValidationException("Another timesheet already exists for week start " + newWeek);
                }
                ts.setWeekStart(newWeek);
            }
        }
        if (dto.getStatus() != null) {
            ts.setStatus(dto.getStatus());
        }

        if (dto.getEntries() != null) {
            // Rebuild entries list from DTO
            ts.getEntries().clear();
            for (TimesheetEntryDTO e : dto.getEntries()) {
                ts.addEntry(buildEntryFromDTO(e, ts));
            }
        }

        ts.recalcTotalHours();
        return timesheetDAO.update(ts);
    }

    public void deleteTimesheet(User currentUser, long id) {
        requireUser(currentUser);
        Timesheet ts = getTimesheet(currentUser, id);
        // getTimesheet enforces owner/admin access
        timesheetDAO.delete(ts.getId());
    }

    // ===== Helpers =====
    private static void requireUser(User u) {
        if (u == null) throw new ValidationException("Current user required");
    }

    private static boolean isOwnerOrAdmin(User current, Timesheet t) {
        if (current == null || t == null) return false;
        if (current.getRole() == UserRole.ADMIN) return true;
        return t.getOwner() != null && t.getOwner().getId() != null && t.getOwner().getId().equals(current.getId());
    }

    private static LocalDate parseWeekStart(String s) {
        if (s == null || s.isBlank()) throw new ValidationException("weekStart is required");
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            throw new ValidationException("Invalid weekStart format, expected yyyy-MM-dd");
        }
    }

    private static TimesheetEntry buildEntryFromDTO(TimesheetEntryDTO dto, Timesheet parent) {
        if (dto == null) return null;
        TimesheetEntry e = new TimesheetEntry();
        // workDate
        if (dto.getWorkDate() == null || dto.getWorkDate().isBlank()) {
            throw new ValidationException("Entry workDate is required");
        }
        try {
            e.setWorkDate(LocalDate.parse(dto.getWorkDate()));
        } catch (Exception ex) {
            throw new ValidationException("Invalid entry workDate format, expected yyyy-MM-dd");
        }
        // project code
        if (dto.getProjectCode() == null || dto.getProjectCode().trim().isEmpty()) {
            throw new ValidationException("Entry projectCode is required");
        }
        e.setProjectCode(dto.getProjectCode().trim());
        e.setTaskCode(dto.getTaskCode());
        // hours
        if (dto.getHours() == null) {
            throw new ValidationException("Entry hours is required");
        }
        BigDecimal hours = dto.getHours();
        if (hours.compareTo(BigDecimal.ZERO) < 0 || hours.compareTo(new BigDecimal("24.00")) > 0) {
            throw new ValidationException("Entry hours must be between 0.00 and 24.00");
        }
        e.setHours(hours);
        e.setDescription(dto.getDescription());
        e.setTimesheet(parent);
        return e;
    }
}
