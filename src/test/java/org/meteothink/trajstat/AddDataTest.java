package org.meteothink.trajstat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.meteothink.trajstat.trajectory.TrajUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddDataTest {

    @Test
    public void testGetTimeZone() {
        int tz = TrajUtil.getTimeZone("GMT+4");
        Assertions.assertEquals(4, tz);
    }

    @Test
    public void testTimeMatch() {
        LocalDateTime dt = LocalDateTime.of(2019, 1, 1, 0, 0);
        dt = dt.withHour(6);
        System.out.println(dt);
        dt = dt.plusHours(4);
        System.out.println(dt);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("d/M/yyyy H:00");
        String dtStr = format.format(dt);
        System.out.println(dtStr);
    }
}
