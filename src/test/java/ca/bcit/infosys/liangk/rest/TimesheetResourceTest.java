package ca.bcit.infosys.liangk.rest;

import ca.bcit.infosys.liangk.dto.TimesheetDTO;
import ca.bcit.infosys.liangk.dto.TimesheetEntryDTO;
import ca.bcit.infosys.liangk.entity.Timesheet;
import ca.bcit.infosys.liangk.entity.TimesheetEntry;
import ca.bcit.infosys.liangk.entity.TimesheetStatus;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.exception.ValidationException;
import ca.bcit.infosys.liangk.security.CurrentUserHolder;
import ca.bcit.infosys.liangk.service.TimesheetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimesheetResourceTest {

    private TimesheetResource resource;
    private FakeTimesheetService service;
    private CurrentUserHolder holder;

    @BeforeEach
    void setup() throws Exception {
        resource = new TimesheetResource();
        service = new FakeTimesheetService();
        holder = new CurrentUserHolder();
        User u = new User(); u.setId(1L); u.setUsername("jdoe"); u.setRole(UserRole.USER);
        holder.setUser(u);
        setField(resource, "timesheetService", service);
        setField(resource, "currentUserHolder", holder);
    }

    @Test
    void list_returnsCreatedItem() {
        // Arrange - create one directly via the service (bypassing Response building)
        TimesheetDTO req = new TimesheetDTO();
        req.setWeekStart("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate("2025-01-06");
        e.setProjectCode("P1");
        e.setHours(new BigDecimal("2.00"));
        req.setEntries(List.of(e));
        service.createTimesheet(holder.getUser(), req);

        var list = resource.list(null);
        assertEquals(1, list.size());
        assertEquals("2025-01-06", list.get(0).getWeekStart());
    }

    @Test
    void get_returnsDTOForExisting() {
        TimesheetDTO req = new TimesheetDTO();
        req.setWeekStart("2025-01-06");
        Timesheet t = service.createTimesheet(holder.getUser(), req);
        TimesheetDTO dto = resource.get(t.getId());
        assertEquals("2025-01-06", dto.getWeekStart());
    }

    @Test
    void create_withInvalidWeek_throwsValidationException() {
        TimesheetDTO req = new TimesheetDTO();
        req.setWeekStart("bad-date");
        assertThrows(ValidationException.class, () -> service.createTimesheet(holder.getUser(), req));
    }

    // ===== Helpers =====

    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // In-memory fake service to simulate persistence and business logic
    private static class FakeTimesheetService extends TimesheetService {
        private final Map<Long, Timesheet> storage = new HashMap<>();
        private long seq = 1L;
        @Override
        public Timesheet createTimesheet(User currentUser, TimesheetDTO dto) {
            if (dto.getWeekStart() == null) throw new ValidationException("weekStart is required");
            LocalDate week = parseDate(dto.getWeekStart());
            Timesheet t = new Timesheet();
            t.setId(seq++);
            t.setOwner(currentUser);
            t.setWeekStart(week);
            t.setStatus(dto.getStatus() == null ? TimesheetStatus.OPEN : dto.getStatus());
            List<TimesheetEntry> entries = new ArrayList<>();
            if (dto.getEntries() != null) {
                for (TimesheetEntryDTO e : dto.getEntries()) {
                    TimesheetEntry entry = new TimesheetEntry();
                    entry.setWorkDate(parseDate(e.getWorkDate()));
                    entry.setProjectCode(e.getProjectCode());
                    entry.setHours(e.getHours());
                    entry.setTimesheet(t);
                    entries.add(entry);
                }
            }
            t.setEntries(entries);
            t.recalcTotalHours();
            storage.put(t.getId(), t);
            return t;
        }
        @Override
        public Timesheet getTimesheet(User currentUser, long id) {
            Timesheet t = storage.get(id);
            if (t == null) throw new ca.bcit.infosys.liangk.exception.NotFoundException("Timesheet not found: id=" + id);
            return t;
        }
        @Override
        public java.util.List<Timesheet> listTimesheets(User currentUser, Optional<LocalDate> weekStart) {
            return storage.values().stream().sorted(Comparator.comparing(Timesheet::getId)).toList();
        }
        private static LocalDate parseDate(String s) {
            try { return LocalDate.parse(s); } catch (Exception ex) { throw new ValidationException("Invalid weekStart format, expected yyyy-MM-dd"); }
        }
    }
}
