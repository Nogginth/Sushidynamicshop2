package mcsushi.dynamicshop.sushidynamicshop.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultCurrencyRegistry implements CurrencyRegistry {

    @Override
    public boolean currencyExists(String currencyId) {
        // Free version ไม่รองรับ Custom Currency
        return false;
    }

    @Override
    public String getPronoun(String currencyId) {
        // ไม่มี Custom Currency ใน Free version
        return "";
    }

    @Override
    public List<String> getAllCurrencies() {
        // ไม่มี Custom Currency ใน Free version
        return new ArrayList<>();
    }

    @Override
    public List<String> getTop10Players(String currencyId, String pronoun) {
        // Free version ไม่มีระบบ Custom Currency
        return new ArrayList<>();
    }

    @Override
    public String getPlayerBalance(UUID playerUUID, String currencyId, String pronoun) {
        // คืนค่า 0 เสมอใน Free version
        return "§e" + playerUUID.toString() + " §7has §a0 §6" + pronoun;
    }

    @Override
    public boolean addCurrency(String currencyId, String pronoun) {
        // Free version ไม่รองรับการเพิ่ม Custom Currency
        return false;
    }

    @Override
    public boolean updateCurrency(String currencyId, String newPronoun) {
        // Free version ไม่รองรับการแก้ไข Custom Currency
        return false;
    }

    @Override
    public boolean adjustPlayerBalance(UUID playerUUID, String currencyId, double amount, String action) {
        // Free version ไม่รองรับการปรับยอดเงิน Custom Currency
        return false;
    }

    @Override
    public boolean removeCurrency(String currencyId) {
        // Free version ไม่รองรับการลบ Custom Currency
        return false;
    }
}
