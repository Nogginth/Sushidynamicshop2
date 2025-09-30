package mcsushi.dynamicshop.sushidynamicshop.hook;

public class MMOItemHook {

    private static boolean hooked = false;

    public static boolean setup() {
        hooked = true;
        return true;
    }

    public static boolean isHooked() {
        return hooked;
    }
}