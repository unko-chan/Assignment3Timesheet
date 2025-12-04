package ca.bcit.infosys.liangk.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TimesheetRecalcTotalHoursTest {

    @Test
    void sumsEntryHoursAndHandlesNulls() {
        Timesheet t = new Timesheet();
        t.setWeekStart(LocalDate.of(2025, 1, 6));

        TimesheetEntry e1 = new TimesheetEntry();
        e1.setWorkDate(LocalDate.of(2025, 1, 6));
        e1.setProjectCode("P1");
        e1.setHours(new BigDecimal("1.25"));

        TimesheetEntry e2 = new TimesheetEntry();
        e2.setWorkDate(LocalDate.of(2025, 1, 7));
        e2.setProjectCode("P2");
        e2.setHours(new BigDecimal("2.75"));

        TimesheetEntry e3 = new TimesheetEntry();
        e3.setWorkDate(LocalDate.of(2025, 1, 8));
        e3.setProjectCode("P3");
        e3.setHours(null); // should be ignored in sum

        t.addEntry(e1);
        t.addEntry(e2);
        t.addEntry(e3);

        assertEquals(new BigDecimal("4.00"), t.getTotalHours());
    }

    @Test
    void emptyList_resultsInZero() {
        Timesheet t = new Timesheet();
        t.setWeekStart(LocalDate.of(2025, 1, 6));
        t.recalcTotalHours();
        assertEquals(new BigDecimal("0"), t.getTotalHours());
    }

    @Test
    void addAndRemoveRecalculates() {
        Timesheet t = new Timesheet();
        t.setWeekStart(LocalDate.of(2025, 1, 6));

        TimesheetEntry e1 = new TimesheetEntry();
        e1.setWorkDate(LocalDate.of(2025, 1, 6));
        e1.setProjectCode("P1");
        e1.setHours(new BigDecimal("4.50"));

        TimesheetEntry e2 = new TimesheetEntry();
        e2.setWorkDate(LocalDate.of(2025, 1, 7));
        e2.setProjectCode("P1");
        e2.setHours(new BigDecimal("1.25"));

        t.addEntry(e1);
        t.addEntry(e2);
        assertEquals(new BigDecimal("5.75"), t.getTotalHours());

        t.removeEntry(e2);
        assertEquals(new BigDecimal("4.50"), t.getTotalHours());
    }
}
