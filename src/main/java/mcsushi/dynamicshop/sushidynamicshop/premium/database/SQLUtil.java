package mcsushi.dynamicshop.sushidynamicshop.premium.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;
import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;

public class SQLUtil {

    private static final Logger logger = Sushidynamicshop.getInstance().getLogger();

    /**
     * สร้าง PreparedStatement โดยมีการจัดการพารามิเตอร์อย่างปลอดภัย
     *
     * @param conn  Connection จาก DatabaseManager
     * @param sql   SQL Query ที่ต้องการรัน
     * @param params ค่าพารามิเตอร์สำหรับ PreparedStatement
     * @return PreparedStatement ที่ถูกเตรียมไว้แล้ว
     * @throws SQLException หากมีข้อผิดพลาดในการสร้าง PreparedStatement
     */
    public static PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }

        return stmt;
    }

    /**
     * ล้างข้อมูล String เพื่อป้องกัน SQL Injection
     *
     * @param input ข้อมูลที่ต้องการล้าง
     * @return ข้อมูลที่ถูกล้างแล้ว
     */
    public static String sanitizeString(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * สร้าง Query พร้อมกับ Debug Logging
     */
    public static String buildQuery(String baseQuery, Object... params) {
        StringBuilder sb = new StringBuilder(baseQuery);
        sb.append(" | Params: ");

        for (Object param : params) {
            sb.append(param).append(", ");
        }

        logger.info("[SQLUtil] " + sb.toString());
        return baseQuery;
    }
}
