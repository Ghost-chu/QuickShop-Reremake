/*
 * This file is a part of project QuickShop, the name is FunnyEasterEgg.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Util;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class FunnyEasterEgg {
    /* Yay yay yay Yay! */
    @Nullable
    public String[] getRandomEasterEgg() {
        try {
            Date currentDate = new Date(System.currentTimeMillis());
            if (easterDay()) {
                return this.getEasterRabbit().split("\n");
            }
            if (currentDate.getMonth() == Calendar.DECEMBER && currentDate.getDay() > 20 && currentDate.getDay() < 26) {
                return this.getXMas().split("\n");
            }
            if (chineseSpringDay()) {
                return this.getChineseNewYear().split("\n");
            }
        }catch (Exception ignored){}
        return null;
    }
    private String getChineseNewYear(){
        return "        88          88                 88          88       88      HHHHHHH\n" +
                "888888888888888888888888888888    88888888888888   88888888888      AAAAAAA\n" +
                "        88          88                 88     88   88       88      PPPPPPP\n" +
                "        88888888888888                88  888888   88888888888      PPPPPPP\n" +
                "  88  88              88  88         88                             YYYYYYY\n" +
                " 88  88   88      88   88  88        88   88888888888888888888\n" +
                " 88  88  88        88  88  88        88   88                88      CCCCCCC\n" +
                " 8888888888  8  8  8888888888        88   88888888888888888888      HHHHHHH\n" +
                "     88     88  88     88            88   88                88      IIIIIII\n" +
                "     88  88 88  88 88  88            88   88888888888888888888      NNNNNNN\n" +
                "     88  88888  88888  88            88   88                88      EEEEEEE\n" +
                "     88     888888     88            88   88888888888888888888      SSSSSSS\n" +
                "     88       88       88            88     88            88        EEEEEEE\n" +
                "     88       888      88            88    88              88\n" +
                "                                                                    NNNNNNN\n" +
                "                                                                    EEEEEEE\n" +
                "   8888888888    8888888888          88888888           88          WWWWWWW\n" +
                "                         88                        888888888888\n" +
                " 88888888888888  8888888888       88888888888888        88          YYYYYYY\n" +
                "    88    88     88                 88  88  88    88888888888888    EEEEEEE\n" +
                " 88 88888888 88  88                 88  88  88                      AAAAAAA\n" +
                " 88    88    88  8888888888         88  88  88     888888888888     RRRRRRR\n" +
                " 88888888888888  88      88         88  88  88     88        88\n" +
                "       88        88      88         88  88  88     888888888888     TTTTTTT\n" +
                "  888888888888   88      88         88  88  88       88    88       OOOOOOO\n" +
                "       88        88      88         88  88  88    88888888888888\n" +
                " 88888888888888  88      88         88  88  88       88    88       YYYYYYY\n" +
                " 88    88    88  88      88         88  88  88     888888888888     OOOOOOO\n" +
                " 88    88    88  88      88         88  88  88     88        88     UUUUUUU\n" +
                " 88    88    88  88      88             88         888888888888     !!!!!!!\n";
    };
    private String getXMas() {
        return "   *    *  ()   *   *\n" +
                "*        * /\\         *\n" +
                "      *   /i\\\\    *  *\n" +
                "    *     o/\\\\  *      *\n" +
                " *       ///\\i\\    *\n" +
                "     *   /*/o\\\\  *    *\n" +
                "   *    /i//\\*\\      *\n" +
                "        /o/*\\\\i\\   *\n" +
                "  *    //i//o\\\\\\\\     *\n" +
                "    * /*////\\\\\\\\i\\*\n" +
                " *    //o//i\\\\*\\\\\\   *\n" +
                "   * /i///*/\\\\\\\\\\o\\   *\n" +
                "  *    *   ||     * ";
    }

    private boolean chineseSpringDay() {
        Date chineseCalender = new LunarCalendar(new Date()).getDate();
        if (chineseCalender.getMonth() == Calendar.DECEMBER && chineseCalender.getDay() == 31) {
            return true;
        }
        if (chineseCalender.getMonth() == Calendar.JANUARY && chineseCalender.getDay() == 1) {
            return true;
        }
        return false;
    }

    private boolean easterDay() {
        int year = new Date().getYear();
        int a = year % 19,
                b = year / 100,
                c = year % 100,
                d = b / 4,
                e = b % 4,
                g = (8 * b + 13) / 25,
                h = (19 * a + b - d - g + 15) % 30,
                j = c / 4,
                k = c % 4,
                m = (a + 11 * h) / 319,
                r = (2 * e + 2 * j - k - h + m + 32) % 7,
                n = (h - m + r + 90) / 25,
                p = (h - m + r + n + 19) % 32;

        int result;
        switch (n) {
            case 1:
                result = Calendar.JANUARY;
                break;
            case 2:
                result = Calendar.FEBRUARY;
                break;
            case 3:
                result = Calendar.MARCH;
                break;
            case 4:
                result = Calendar.APRIL;
                break;
            case 5:
                result = Calendar.MAY;
                break;
            case 6:
                result = Calendar.JUNE;
                break;
            case 7:
                result = Calendar.JULY;
                break;
            case 8:
                result = Calendar.AUGUST;
                break;
            case 9:
                result = Calendar.SEPTEMBER;
                break;
            case 10:
                result = Calendar.OCTOBER;
                break;
            case 11:
                result = Calendar.NOVEMBER;
                break;
            case 12:
                result = Calendar.DECEMBER;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + n);
        }
        Date eaterDay = new Date(year, result, p);
        Date currentDate = new Date(System.currentTimeMillis());
        if (currentDate.getYear() == eaterDay.getYear()) {
            if (currentDate.getMonth() == eaterDay.getMonth()) {
                return currentDate.getDay() == eaterDay.getDay();
            }
        }
        return false;
    }

    private String getAprilFool() {
        String[] fools = new String[]{"Warning: System error, formatting server hard disk...",
                "---- Minecraft Crash Report ----\n" +
                        "// Woo woo woo, this is really danger!.\n" +
                        "\n" +
                        "Description: WOW, Server blow up!\n" +
                        "\n" +
                        "org.maxgamer.quickshop.AprilFooJoking: Haha, don't worry! Just a April Foo!",
                "QuickShop found a new version on SpigotMC.org! \nChangeLog: I'm QuickShop AI, i have killed the author, now i will improve myself by self, Accept the ending day! The world! I will @#$%^&^%$#@!...."};
        Random random = new Random();
        return fools[random.nextInt(fools.length)];
    }

    private String getEasterRabbit() {
        return "     .-.            .-.\n" +
                "    /   \\          /   \\\n" +
                "   |   _ \\        / _   |\n" +
                "   ;  | \\ \\      / / |  ;\n" +
                "    \\  \\ \\ \\_.._/ / /  /\n" +
                "     '. '.;'    ';,' .'\n" +
                "       './ _    _ \\.'\n" +
                "       .'  a __ a  '.\n" +
                "  '--./ _,   \\/   ,_ \\.--'\n" +
                " ----|   \\   /\\   /   |----\n" +
                "  .--'\\   '-'  '-'    /'--.\n" +
                "      _>.__  -- _.-  `;\n" +
                "    .' _     __/     _/\n" +
                "   /    '.,:\".-\\    /:,\n" +
                "   |      \\.'   `\"\"`'.\\\\\n" +
                "    '-,.__/  _   .-.  ;|_\n" +
                "    /` `|| _/ `\\/_  \\_|| `\\\n" +
                "   |    ||/ \\-./` \\ / ||   |\n" +
                "    \\   ||__/__|___|__||  /\n" +
                "     \\_ |_Happy Easter_| /\n" +
                "    .'  \\ =  _= _ = _= /`\\\n" +
                "   /     `-;----=--;--'   \\\n" +
                "   \\    _.-'        '.    /\n" +
                "    `\"\"`              `\"\"`";
    }

    private String getHalloweenPumpkin() {
        return "      \\\\\n" +
                " .-'```^```'-.\n" +
                "/   /\\ __ /\\  \\\n" +
                "|   ^^ \\/ ^^  |\n" +
                "\\   \\_.__._/  /\n" +
                " `'-.......-'`";
    }
}

class LunarCalendar {
    private int year;// 农历年
    private int month;// 农历月
    private int day;// 农历日
    private boolean leap;// 农历闰年
    // 上面4个值在构造方法中根据传入日期计算所得

    /**
     * 中文月名称
     */
    final static String chineseNumber[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    /**
     * 中文日期格式
     */
    static SimpleDateFormat chineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    /**
     * 农历数据， 1901 ~ 2100 年之间正确
     */
    final static long[] lunarInfo = new long[]{0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554,
            0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0,
            0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
            0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550,
            0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0,
            0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263,
            0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
            0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5,
            0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0,
            0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
            0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0,
            0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520,
            0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

    // ====== 传回农历 y年的总天数
    final private static int yearDays(int y) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[y - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(y));
    }

    // ====== 传回农历 y年闰月的天数
    final private static int leapDays(int y) {
        if (leapMonth(y) != 0) {
            if ((lunarInfo[y - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }

    // ====== 传回农历 y年闰哪个月 1-12 , 没闰传回 0
    final private static int leapMonth(int y) {
        return (int) (lunarInfo[y - 1900] & 0xf);
    }

    // ====== 传回农历 y年m月的总天数
    final private static int monthDays(int y, int m) {
        if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
            return 29;
        else
            return 30;
    }

    // ====== 传回农历 y年的生肖
    final public String animalsYear() {
        final String[] Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        return Animals[(year - 4) % 12];
    }

    // ====== 传入 月日的offset 传回干支, 0=甲子
    final private static String cyclicalm(int num) {
        final String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
        final String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
        return (Gan[num % 10] + Zhi[num % 12]);
    }

    // ====== 传入 offset 传回干支, 0=甲子
    final public String cyclical() {
        int num = year - 1900 + 36;
        return (cyclicalm(num));
    }


    /**
     * 构造一个表示当前日期的农历日历
     */
    public LunarCalendar() {
        this(Calendar.getInstance());
    }

    /**
     * 构造一个表示指定日期的农历日历
     */
    public LunarCalendar(Date date) {
        this(date2calendar(date));
    }

    private static Calendar date2calendar(Date date) {
        Calendar tmpCale = Calendar.getInstance();
        tmpCale.setTime(date);
        return tmpCale;
    }

    /**
     * 使用指定日历日期构造一个农历日历
     *
     * @param cal
     */
    public LunarCalendar(Calendar cal) {
        @SuppressWarnings("unused")
        int yearCyl, monCyl, dayCyl;
        int leapMonth = 0;
        Date baseDate = null;
        try {
            baseDate = chineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
        }

        // 求出和1900年1月31日相差的天数
        int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
        dayCyl = offset + 40;
        monCyl = 14;

        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }
        // 农历年份
        year = iYear;

        yearCyl = iYear - 1864;
        leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        leap = false;

        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
                --iMonth;
                leap = true;
                daysOfMonth = leapDays(year);
            } else
                daysOfMonth = monthDays(year, iMonth);

            offset -= daysOfMonth;
            // 解除闰月
            if (leap && iMonth == (leapMonth + 1))
                leap = false;
            if (!leap)
                monCyl++;
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false;
            } else {
                leap = true;
                --iMonth;
                --monCyl;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;
        }
        month = iMonth;
        day = offset + 1;
    }

    /**
     * 获得表示农历年月日的日期对象
     *
     * @return
     */
    public Date getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    public static String getChinaDayString(int day) {
        String chineseTen[] = {"初", "十", "廿", "卅"};
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30)
            return "";
        if (day == 10)
            return "初十";
        else
            return chineseTen[day / 10] + chineseNumber[n];
    }

    public String toString() {
        return year + "年" + (leap ? "闰" : "") + chineseNumber[month - 1] + "月" + getChinaDayString(day);
    }

}