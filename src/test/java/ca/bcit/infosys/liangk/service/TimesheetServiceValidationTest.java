package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.TimesheetDAO;
import ca.bcit.infosys.liangk.dto.TimesheetDTO;
import ca.bcit.infosys.liangk.dto.TimesheetEntryDTO;
import ca.bcit.infosys.liangk.entity.Timesheet;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimesheetServiceValidationTest {

    private TimesheetService service;
    private FakeTimesheetDAO fakeDAO;
    private User owner;

    @BeforeEach
    void setup() throws Exception {
        service = new TimesheetService();
        fakeDAO = new FakeTimesheetDAO();
        setField(service, "timesheetDAO", fakeDAO);
        owner = new User(); owner.setId(1L); owner.setRole(UserRole.USER);
    }

    @Test
    void entryHours_negative_rejected() {
        TimesheetDTO dto = baseDTO("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate("2025-01-06");
        e.setProjectCode("P");
        e.setHours(new BigDecimal("-0.25"));
        dto.setEntries(List.of(e));
        assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
    }

    @Test
    void entryHours_over24_rejected() {
        TimesheetDTO dto = baseDTO("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate("2025-01-06");
        e.setProjectCode("P");
        e.setHours(new BigDecimal("24.25"));
        dto.setEntries(List.of(e));
        assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
    }

    @Test
    void entryWorkDate_missing_rejected() {
        TimesheetDTO dto = baseDTO("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate(null);
        e.setProjectCode("P");
        e.setHours(new BigDecimal("1.00"));
        dto.setEntries(List.of(e));
        assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
    }

    @Test
    void entryWorkDate_invalidFormat_rejected() {
        TimesheetDTO dto = baseDTO("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate("06-01-2025"); // wrong format
        e.setProjectCode("P");
        e.setHours(new BigDecimal("1.00"));
        dto.setEntries(List.of(e));
        assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
    }

    @Test
    void projectCode_blank_rejected() {
        TimesheetDTO dto = baseDTO("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate("2025-01-06");
        e.setProjectCode("   ");
        e.setHours(new BigDecimal("1.00"));
        dto.setEntries(List.of(e));
        assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
    }

    private static TimesheetDTO baseDTO(String weekStart) {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setWeekStart(weekStart);
        return dto;
    }

    private static void setField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // Minimal in-memory DAO to satisfy service
    private static class FakeTimesheetDAO extends TimesheetDAO {
        Map<Long, Timesheet> storage = new HashMap<>();
        long seq = 1L;
        @Override public Timesheet findById(long id) { return storage.get(id); }
        @Override public Timesheet findByUserAndWeek(long userId, LocalDate weekStart) {
            return storage.values().stream().filter(t -> t.getOwner()!=null && Objects.equals(t.getOwner().getId(), userId) && weekStart.equals(t.getWeekStart())).findFirst().orElse(null);
        }
        @Override public List<Timesheet> findByUser(long userId) {
            return storage.values().stream().filter(t -> t.getOwner()!=null && Objects.equals(t.getOwner().getId(), userId)).toList();
        }
        @Override public Timesheet create(Timesheet timesheet) { timesheet.setId(seq++); storage.put(timesheet.getId(), timesheet); return timesheet; }
        @Override public Timesheet update(Timesheet timesheet) { storage.put(timesheet.getId(), timesheet); return timesheet; }
        @Override public void delete(long id) { storage.remove(id); }
    }
}
