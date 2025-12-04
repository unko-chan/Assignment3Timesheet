package ca.bcit.infosys.liangk.util;

import ca.bcit.infosys.liangk.dto.*;
import ca.bcit.infosys.liangk.entity.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Mapper {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private Mapper() {}

    // ===== Entity -> DTO =====
    public static UserDTO toUserDTO(User u) {
        if (u == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        dto.setEmployeeNumber(u.getEmployeeNumber());
        dto.setRole(u.getRole());
        dto.setActive(u.isActive());
        return dto;
    }

    public static TimesheetEntryDTO toTimesheetEntryDTO(TimesheetEntry e) {
        if (e == null) return null;
        TimesheetEntryDTO dto = new TimesheetEntryDTO();
        dto.setId(e.getId());
        dto.setWorkDate(e.getWorkDate() == null ? null : e.getWorkDate().format(ISO_DATE));
        dto.setProjectCode(e.getProjectCode());
        dto.setTaskCode(e.getTaskCode());
        dto.setHours(e.getHours());
        dto.setDescription(e.getDescription());
        return dto;
    }

    public static TimesheetDTO toTimesheetDTO(Timesheet t) {
        if (t == null) return null;
        TimesheetDTO dto = new TimesheetDTO();
        dto.setId(t.getId());
        dto.setWeekStart(t.getWeekStart() == null ? null : t.getWeekStart().format(ISO_DATE));
        dto.setStatus(t.getStatus());
        dto.setTotalHours(t.getTotalHours());
        List<TimesheetEntryDTO> entryDTOs = new ArrayList<>();
        if (t.getEntries() != null) {
            for (TimesheetEntry e : t.getEntries()) {
                entryDTOs.add(toTimesheetEntryDTO(e));
            }
        }
        dto.setEntries(entryDTOs);
        return dto;
    }

    // ===== Helper builders for services (no hashing/DB here) =====
    public static User buildUserFromCreate(CreateUserRequest req) {
        if (req == null) return null;
        User u = new User();
        u.setUsername(req.getUsername());
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setEmployeeNumber(req.getEmployeeNumber());
        u.setRole(req.getRole() == null ? UserRole.USER : req.getRole());
        u.setActive(req.getActive() == null ? true : req.getActive());
        // Password hash must be set by service after hashing plain password
        return u;
    }

    public static void applyUpdateToUser(User user, UpdateUserRequest req) {
        if (user == null || req == null) return;
        if (req.getUsername() != null) user.setUsername(req.getUsername());
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getEmployeeNumber() != null) user.setEmployeeNumber(req.getEmployeeNumber());
        if (req.getRole() != null) user.setRole(req.getRole());
        if (req.getActive() != null) user.setActive(req.getActive());
        // Password is handled by service (hashing), not here
    }
}
