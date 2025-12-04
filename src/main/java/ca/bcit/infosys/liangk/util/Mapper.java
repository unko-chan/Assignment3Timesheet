package ca.bcit.infosys.liangk.util;

import ca.bcit.infosys.liangk.dto.*;
import ca.bcit.infosys.liangk.entity.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility mapper for converting between entities and DTOs.
 */
public final class Mapper {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private Mapper() {}

    // ===== Entity -> DTO =====
    /**
     * Maps a User entity to a UserDTO.
     *
     * @param u User entity
     * @return corresponding UserDTO or null if input is null
     */
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

    /**
     * Maps a TimesheetEntry entity to its DTO representation.
     *
     * @param e TimesheetEntry entity
     * @return TimesheetEntryDTO or null if input is null
     */
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

    /**
     * Maps a Timesheet entity to its DTO representation including its entries.
     *
     * @param t Timesheet entity
     * @return TimesheetDTO or null if input is null
     */
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
    /**
     * Constructs a new User entity from a CreateUserRequest. Does not set the password.
     * The service layer is responsible for setting and validating the password.
     *
     * @param req create request
     * @return partially populated User or null if request is null
     */
    public static User buildUserFromCreate(CreateUserRequest req) {
        if (req == null) return null;
        User u = new User();
        u.setUsername(req.getUsername());
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setEmployeeNumber(req.getEmployeeNumber());
        u.setRole(req.getRole() == null ? UserRole.USER : req.getRole());
        u.setActive(req.getActive() == null ? true : req.getActive());
        // Password must be set by service layer (stored as plaintext per current requirement)
        return u;
    }

    /**
     * Applies non-sensitive updates from an UpdateUserRequest onto an existing User entity.
     * The password must be updated by the service layer explicitly.
     *
     * @param user target entity to mutate
     * @param req  incoming updates
     */
    public static void applyUpdateToUser(User user, UpdateUserRequest req) {
        if (user == null || req == null) return;
        if (req.getUsername() != null) user.setUsername(req.getUsername());
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getEmployeeNumber() != null) user.setEmployeeNumber(req.getEmployeeNumber());
        if (req.getRole() != null) user.setRole(req.getRole());
        if (req.getActive() != null) user.setActive(req.getActive());
        // Password is handled by service, not here
    }
}
