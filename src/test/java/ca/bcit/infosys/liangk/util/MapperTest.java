package ca.bcit.infosys.liangk.util;

import ca.bcit.infosys.liangk.dto.*;
import ca.bcit.infosys.liangk.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTest {

    @Test
    void toUserDTO_mapsAllFields() {
        User u = new User();
        u.setId(5L);
        u.setUsername("jdoe");
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setEmployeeNumber(123);
        u.setRole(UserRole.ADMIN);
        u.setActive(true);

        UserDTO dto = Mapper.toUserDTO(u);
        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("jdoe", dto.getUsername());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals(123, dto.getEmployeeNumber());
        assertEquals(UserRole.ADMIN, dto.getRole());
        assertTrue(dto.isActive());
    }

    @Test
    void toTimesheetEntryDTO_mapsFieldsAndFormatsDate() {
        TimesheetEntry e = new TimesheetEntry();
        e.setId(7L);
        e.setWorkDate(LocalDate.of(2025, 1, 2));
        e.setProjectCode("P100");
        e.setTaskCode("T1");
        e.setHours(new BigDecimal("7.50"));
        e.setDescription("Feature work");

        TimesheetEntryDTO dto = Mapper.toTimesheetEntryDTO(e);
        assertNotNull(dto);
        assertEquals(7L, dto.getId());
        assertEquals("2025-01-02", dto.getWorkDate());
        assertEquals("P100", dto.getProjectCode());
        assertEquals("T1", dto.getTaskCode());
        assertEquals(new BigDecimal("7.50"), dto.getHours());
        assertEquals("Feature work", dto.getDescription());
    }

    @Test
    void toTimesheetDTO_mapsNestedEntriesAndFormatsWeek() {
        Timesheet t = new Timesheet();
        t.setId(9L);
        t.setWeekStart(LocalDate.of(2025, 1, 6));
        t.setStatus(TimesheetStatus.OPEN);

        TimesheetEntry e1 = new TimesheetEntry();
        e1.setId(1L);
        e1.setWorkDate(LocalDate.of(2025, 1, 6));
        e1.setProjectCode("P1");
        e1.setHours(new BigDecimal("2.00"));
        t.addEntry(e1);

        TimesheetEntry e2 = new TimesheetEntry();
        e2.setId(2L);
        e2.setWorkDate(LocalDate.of(2025, 1, 7));
        e2.setProjectCode("P1");
        e2.setHours(new BigDecimal("3.25"));
        t.addEntry(e2);

        TimesheetDTO dto = Mapper.toTimesheetDTO(t);
        assertNotNull(dto);
        assertEquals(9L, dto.getId());
        assertEquals("2025-01-06", dto.getWeekStart());
        assertEquals(TimesheetStatus.OPEN, dto.getStatus());
        assertEquals(new BigDecimal("5.25"), dto.getTotalHours());
        assertEquals(2, dto.getEntries().size());
        assertEquals("2025-01-06", dto.getEntries().get(0).getWorkDate());
    }

    @Test
    void nullInputs_returnNullOrNoop() {
        assertNull(Mapper.toUserDTO(null));
        assertNull(Mapper.toTimesheetDTO(null));
        assertNull(Mapper.toTimesheetEntryDTO(null));

        // applyUpdateToUser should not throw on nulls
        Mapper.applyUpdateToUser(null, null);
    }

    @Test
    void buildUserFromCreate_setsDefaults() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("jdoe");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmployeeNumber(42);
        // role null => default USER; active null => default true

        User u = Mapper.buildUserFromCreate(req);
        assertNotNull(u);
        assertEquals("jdoe", u.getUsername());
        assertEquals("John", u.getFirstName());
        assertEquals("Doe", u.getLastName());
        assertEquals(42, u.getEmployeeNumber());
        assertEquals(UserRole.USER, u.getRole());
        assertTrue(u.isActive());
    }

    @Test
    void applyUpdateToUser_overwritesOnlyProvidedFields() {
        User u = new User();
        u.setUsername("old");
        u.setFirstName("Old");
        u.setLastName("Name");
        u.setEmployeeNumber(1);
        u.setRole(UserRole.USER);
        u.setActive(true);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setUsername("new");
        req.setFirstName("New");
        req.setEmployeeNumber(2);
        req.setRole(UserRole.ADMIN);
        req.setActive(false);

        Mapper.applyUpdateToUser(u, req);
        assertEquals("new", u.getUsername());
        assertEquals("New", u.getFirstName());
        assertEquals("Name", u.getLastName()); // unchanged
        assertEquals(2, u.getEmployeeNumber());
        assertEquals(UserRole.ADMIN, u.getRole());
        assertFalse(u.isActive());
    }
}
