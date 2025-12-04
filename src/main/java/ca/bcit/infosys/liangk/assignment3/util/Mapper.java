package ca.bcit.infosys.liangk.assignment3.util;

import ca.bcit.infosys.liangk.assignment3.dto.*;
import ca.bcit.infosys.liangk.assignment3.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Mapper {
    private Mapper() {}

    // User mappings
    public static UserDTO toUserDTO(User entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmployeeNumber(entity.getEmployeeNumber());
        dto.setRole(entity.getRole());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static User toUserEntity(CreateUserRequest dto) {
        if (dto == null) return null;
        User u = new User();
        u.setUsername(dto.getUsername());
        // The service layer should hash the password; here we accept raw and set passwordHash directly
        // or leave it to service. Set as-is to keep mapping utility simple; service may override.
        if (dto.getPassword() != null) {
            u.setPasswordHash(dto.getPassword());
        }
        u.setFirstName(dto.getFirstName());
        u.setLastName(dto.getLastName());
        u.setEmployeeNumber(dto.getEmployeeNumber());
        if (dto.getRole() != null) {
            u.setRole(dto.getRole());
        }
        if (dto.getActive() != null) {
            u.setActive(dto.getActive());
        }
        return u;
    }

    public static void updateUserEntity(User entity, UpdateUserRequest dto) {
        if (entity == null || dto == null) return;
        if (dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if (dto.getPassword() != null) entity.setPasswordHash(dto.getPassword());
        if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getEmployeeNumber() != null) entity.setEmployeeNumber(dto.getEmployeeNumber());
        if (dto.getRole() != null) entity.setRole(dto.getRole());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
    }

    // Timesheet mappings
    public static TimesheetDTO toTimesheetDTO(Timesheet entity) {
        if (entity == null) return null;
        TimesheetDTO dto = new TimesheetDTO();
        dto.setId(entity.getId());
        dto.setWeekStart(entity.getWeekStart());
        dto.setStatus(entity.getStatus());
        dto.setTotalHours(entity.getTotalHours());
        dto.setUserId(entity.getOwner() != null ? entity.getOwner().getId() : null);

        List<TimesheetEntryDTO> entryDTOs = new ArrayList<>();
        if (entity.getEntries() != null) {
            for (TimesheetEntry e : entity.getEntries()) {
                TimesheetEntryDTO ed = toTimesheetEntryDTO(e);
                if (ed != null) entryDTOs.add(ed);
            }
        }
        dto.setEntries(entryDTOs);
        return dto;
    }

    public static TimesheetEntryDTO toTimesheetEntryDTO(TimesheetEntry entity) {
        if (entity == null) return null;
        TimesheetEntryDTO dto = new TimesheetEntryDTO();
        dto.setId(entity.getId());
        dto.setEntryDate(entity.getEntryDate());
        dto.setProjectCode(entity.getProjectCode());
        dto.setTaskCode(entity.getTaskCode());
        dto.setHours(entity.getHours());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    public static TimesheetEntry toTimesheetEntryEntity(TimesheetEntryDTO dto, Timesheet parent) {
        if (dto == null) return null;
        TimesheetEntry e = new TimesheetEntry();
        e.setTimesheet(parent);
        e.setEntryDate(dto.getEntryDate());
        e.setProjectCode(dto.getProjectCode());
        e.setTaskCode(dto.getTaskCode());
        e.setHours(dto.getHours());
        e.setDescription(dto.getDescription());
        return e;
    }
}
