package mcsushi.dynamicshop.sushidynamicshop.util;

import java.util.List;
import java.util.UUID;

public interface CurrencyRegistry {

    /**
     * ตรวจสอบว่าสกุลเงินนั้นมีอยู่หรือไม่
     * @param currencyId รหัสสกุลเงิน
     * @return true หากมีอยู่
     */
    boolean currencyExists(String currencyId);

    /**
     * ดึง Pronoun ของสกุลเงิน
     * @param currencyId รหัสสกุลเงิน
     * @return Pronoun ของสกุลเงินนั้น หรือ "" หากไม่พบ
     */
    String getPronoun(String currencyId);

    /**
     * ดึงรายการสกุลเงินทั้งหมด
     * @return รายการสกุลเงินทั้งหมด
     */
    List<String> getAllCurrencies();

    /**
     * ดึงข้อมูล Top 10 Players ของสกุลเงินนั้น
     * @param currencyId รหัสสกุลเงิน
     * @param pronoun Pronoun ของสกุลเงิน
     * @return รายชื่อและจำนวนเงินของผู้เล่นสูงสุด 10 คน
     */
    List<String> getTop10Players(String currencyId, String pronoun);

    /**
     * ดึงยอดเงินของผู้เล่นในสกุลเงินนั้น
     * @param playerUUID UUID ของผู้เล่น
     * @param currencyId รหัสสกุลเงิน
     * @param pronoun Pronoun ของสกุลเงิน
     * @return ข้อความในรูปแบบ "<Player> has <Amount> <Pronoun>"
     */
    String getPlayerBalance(UUID playerUUID, String currencyId, String pronoun);

    /**
     * เพิ่มสกุลเงินใหม่
     * @param currencyId รหัสสกุลเงิน
     * @param pronoun หน่วยของสกุลเงิน
     * @return true หากเพิ่มสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    boolean addCurrency(String currencyId, String pronoun);

    /**
     * อัปเดต pronoun ของสกุลเงิน
     * @param currencyId รหัสสกุลเงิน
     * @param newPronoun หน่วยใหม่ของสกุลเงิน
     * @return true หากอัปเดตสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    boolean updateCurrency(String currencyId, String newPronoun);

    /**
     * ปรับยอดเงินของผู้เล่น
     * @param playerUUID UUID ของผู้เล่น
     * @param currencyId รหัสสกุลเงิน
     * @param amount จำนวนเงินที่ต้องการปรับ
     * @param action ประเภทการปรับ (give, remove, set)
     * @return true หากสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    boolean adjustPlayerBalance(UUID playerUUID, String currencyId, double amount, String action);

    /**
     * ลบสกุลเงินออกจากระบบ
     * @param currencyId รหัสสกุลเงินที่ต้องการลบ
     * @return true หากลบสำเร็จ, false หากเกิดข้อผิดพลาด
     */
    boolean removeCurrency(String currencyId);
}
