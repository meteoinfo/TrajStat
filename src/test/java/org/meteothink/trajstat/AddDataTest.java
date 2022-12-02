package org.meteothink.trajstat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geo.mapdata.MapDataManage;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.table.AttributeTable;
import org.meteothink.trajstat.trajectory.TrajUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AddDataTest {

    @Test
    public void testGetTimeZone() {
        int tz = TrajUtil.getTimeZone("GMT+4");
        Assertions.assertEquals(tz, 4);
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

    @Test
    public void testAddData() throws Exception {
        String dataFn = "D:/Temp/traj/test/AL_Waziriya2019inPPMafterremovingdustydays1.csv";
        File dataFile = new File(dataFn);
        int sDateFldIdx = 0;
        String formatStr = "d/M/yyyy H:00";
        int timeZone = 4;
        int dataFldIdx = 5;
        double undef = -9999;
        String fldName = "PM25";
        DataType dataType = DataType.DOUBLE;
        int fLen = 10;
        int fDec = 2;

        String shpFn = "D:/Temp/traj/test/201901mainversion.shp";
        VectorLayer layer = MapDataManage.readMapFile_ShapeFile(shpFn);
        List<VectorLayer> layers = new ArrayList<>();
        layers.add(layer);

        TrajUtil.addDataToTraj(dataFile, sDateFldIdx,formatStr, timeZone, dataFldIdx, undef,
                layers, fldName, dataType, fLen, fDec);
        AttributeTable table = layers.get(0).getAttributeTable();
        System.out.println(table.toString());
    }
}
