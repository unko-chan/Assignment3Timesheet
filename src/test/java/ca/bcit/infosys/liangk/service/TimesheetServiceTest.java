package ca.bcit.infosys.liangk.service;

import ca.bcit.infosys.liangk.dao.TimesheetDAO;
import ca.bcit.infosys.liangk.dto.TimesheetDTO;
import ca.bcit.infosys.liangk.dto.TimesheetEntryDTO;
import ca.bcit.infosys.liangk.entity.Timesheet;
import ca.bcit.infosys.liangk.entity.TimesheetStatus;
import ca.bcit.infosys.liangk.entity.User;
import ca.bcit.infosys.liangk.entity.UserRole;
import ca.bcit.infosys.liangk.exception.ForbiddenException;
import ca.bcit.infosys.liangk.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class TimesheetServiceTest {

    private TimesheetService service;
    private FakeTimesheetDAO fakeDAO;
    private User owner;
    private User otherUser;
    private User admin;

    @BeforeEach
    void setup() throws Exception {
        service = new TimesheetService();
        fakeDAO = new FakeTimesheetDAO();
        setField(service, "timesheetDAO", fakeDAO);

        owner = new User(); owner.setId(1L); owner.setRole(UserRole.USER);
        otherUser = new User(); otherUser.setId(2L); otherUser.setRole(UserRole.USER);
        admin = new User(); admin.setId(3L); admin.setRole(UserRole.ADMIN);
    }

    @Test
    void createTimesheet_successAndRecalc() {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setWeekStart("2025-01-06");
        dto.setStatus(TimesheetStatus.OPEN);

        TimesheetEntryDTO e1 = new TimesheetEntryDTO();
        e1.setWorkDate("2025-01-06");
        e1.setProjectCode("P1");
        e1.setHours(new BigDecimal("2.50"));

        TimesheetEntryDTO e2 = new TimesheetEntryDTO();
        e2.setWorkDate("2025-01-08");
        e2.setProjectCode("P2");
        e2.setHours(new BigDecimal("3.25"));

        dto.setEntries(Arrays.asList(e1, e2));

        Timesheet created = service.createTimesheet(owner, dto);
        assertNotNull(created.getId());
        assertEquals(new BigDecimal("5.75"), created.getTotalHours());
        assertEquals(2, created.getEntries().size());

        // DAO should have it stored
        assertEquals(1, fakeDAO.storage.size());
    }

    @Test
    void createTimesheet_duplicateWeekRejected() {
        // First create
        createBasicWeek(owner, LocalDate.of(2025,1,6));
        // Attempt duplicate week
        TimesheetDTO dto = new TimesheetDTO();
        dto.setWeekStart("2025-01-06");
        ValidationException ex = assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
    }

    @Test
    void createTimesheet_entryOutsideWeekRejected() {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setWeekStart("2025-01-06");
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate("2025-01-14"); // outside week
        e.setProjectCode("P");
        e.setHours(new BigDecimal("1.00"));
        dto.setEntries(Collections.singletonList(e));
        assertThrows(ValidationException.class, () -> service.createTimesheet(owner, dto));
    }

    @Test
    void getTimesheet_forbiddenForOtherUserButAllowedForAdmin() {
        Timesheet ts = createBasicWeek(owner, LocalDate.of(2025,1,6));
        // Other regular user should be forbidden
        assertThrows(ForbiddenException.class, () -> service.getTimesheet(otherUser, ts.getId()));
        // Admin allowed
        Timesheet fetched = service.getTimesheet(admin, ts.getId());
        assertEquals(ts.getId(), fetched.getId());
    }

    @Test
    void updateTimesheet_conflictingWeekRejected() {
        Timesheet ts1 = createBasicWeek(owner, LocalDate.of(2025,1,6));
        Timesheet ts2 = createBasicWeek(owner, LocalDate.of(2025,1,13));

        TimesheetDTO update = new TimesheetDTO();
        update.setWeekStart("2025-01-06"); // conflicts with ts1
        assertThrows(ValidationException.class, () -> service.updateTimesheet(owner, ts2.getId(), update));
    }

    @Test
    void listTimesheets_filterByWeekStart() {
        Timesheet ts1 = createBasicWeek(owner, LocalDate.of(2025,1,6));
        createBasicWeek(owner, LocalDate.of(2025,1,13));

        var list = service.listTimesheets(owner, Optional.of(LocalDate.of(2025,1,6)));
        assertEquals(1, list.size());
        assertEquals(ts1.getId(), list.get(0).getId());
    }

    // Helpers
    private Timesheet createBasicWeek(User u, LocalDate weekStart) {
        TimesheetDTO dto = new TimesheetDTO();
        dto.setWeekStart(weekStart.toString());
        TimesheetEntryDTO e = new TimesheetEntryDTO();
        e.setWorkDate(weekStart.toString());
        e.setProjectCode("P");
        e.setHours(new BigDecimal("1.00"));
        dto.setEntries(Collections.singletonList(e));
        return service.createTimesheet(u, dto);
    }

    private static void setField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // In-memory fake DAO
    private static class FakeTimesheetDAO extends TimesheetDAO {
        Map<Long, Timesheet> storage = new HashMap<>();
        AtomicLong seq = new AtomicLong(1);

        @Override
        public Timesheet findById(long id) {
            return storage.get(id);
        }

        @Override
        public Timesheet findByUserAndWeek(long userId, LocalDate weekStart) {
            return storage.values().stream()
                    .filter(t -> t.getOwner() != null && Objects.equals(t.getOwner().getId(), userId)
                            && weekStart.equals(t.getWeekStart()))
                    .findFirst().orElse(null);
        }

        @Override
        public java.util.List<Timesheet> findByUser(long userId) {
            return storage.values().stream()
                    .filter(t -> t.getOwner() != null && Objects.equals(t.getOwner().getId(), userId))
                    .sorted(Comparator.comparing(Timesheet::getWeekStart).reversed())
                    .toList();
        }

        @Override
        public Timesheet create(Timesheet timesheet) {
            long id = seq.getAndIncrement();
            timesheet.setId(id);
            storage.put(id, timesheet);
            return timesheet;
        }

        @Override
        public Timesheet update(Timesheet timesheet) {
            storage.put(timesheet.getId(), timesheet);
            return timesheet;
        }

        @Override
        public void delete(long id) {
            storage.remove(id);
        }
    }
}
