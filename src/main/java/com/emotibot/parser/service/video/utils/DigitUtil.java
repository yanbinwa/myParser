package com.emotibot.parser.service.video.utils;
import java.util.*;

/**
 * 处理数词的工具类
 * @author liushiquan
 *
 */
public class DigitUtil {
    /**
     * 阿拉伯数字
     */
    private static Set<Character> araDigits = new HashSet<Character>();
    /**
     * 汉字中的数字字符
     */
    private static char[] SCDigits = {'零','一','二','三','四','五','六','七','八','九','十','百','千','万','亿'};

    /**
     * 汉字中的大写数字字符
     */
    private static char[] TCDigits = {'零','壹','贰','叁','肆','伍','陆','柒','捌','玖','拾','佰','仟','万','亿'};
    /**
     * 繁体中文和简体中文的对应关系
     */
    
    static String[] units = { "", "十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿", "百亿", "千亿", "万亿" };
    
    private static Map<Character,Character> map = new HashMap<Character,Character>();
    static {
        for (int i = 0; i < TCDigits.length; i++) {
            map.put(TCDigits[i], SCDigits[i]);
        }
        for (char i = '0'; i <= '9'; i++) {
            araDigits.add(i);
        }
    }
    private DigitUtil(){

    }
    public static void main(String[] args) {
        System.out.println(parseDigits("一"));
    }
    /**
     * 解析中文格式的数字，假定参数中全是汉字，否则会解析异常，解析失败返回null
     * @param hanzi
     * @return
     */
    public static Integer parseDigits(String hanzi) {
        if (!isDigits(hanzi))
            return null;
        int ret;
        try {
            if (hanzi.charAt(0) == '+')
                hanzi = hanzi.substring(1);

            ret = Integer.parseInt(hanzi);
        } catch (Exception e) {

            char[] chars = hanzi.toCharArray();
            changeTCtoSC(chars);

            ret = parse(chars,0,chars.length,1);
        }

        return ret;
    }
    public static boolean isDigits(String s) {
        if (s.charAt(0) == '+')
            s = s.substring(1);
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (!map.values().contains(c) && !araDigits.contains(c))
                    return false;
            }

            return true;
        }
    }
    private static int parse(char[] chars,int start,int end, int preNumber) {
        int ret = 0;
        if (start == end) {
            ret = 0;
        } else if (start + 1 == end) {
            switch (chars[start]) {
            case '零':
            case '0':
                ret = 0 * preNumber;
                break;
            case '一':
            case '1':
                ret = 1 * preNumber;
                break;
            case '两':
            case '二':
            case '2':
                ret = 2 * preNumber;
                break;
            case '三':
            case '3':
                ret = 3 * preNumber;
                break;
            case '四':
            case '4':
                ret = 4 * preNumber;
                break;
            case '五':
            case '5':
                ret = 5 * preNumber;
                break;
            case '六':
            case '6':
                ret = 6 * preNumber;
                break;
            case '七':
            case '7':
                ret = 7 * preNumber;
                break;
            case '八':
            case '8':
                ret = 8 * preNumber;
                break;
            case '九':
            case '9':
                ret = 9 * preNumber;
                break;
            case '十':
                ret = 10 * preNumber;
                break;
            }
        } else {
            int index;
            if ((index = indexOf(chars,start,end,'零')) == 0 || (index = indexOf(chars,start,end,'0')) == 0) {
                ret = parse(chars, start + 1, end, 1);
            }else if ((index = indexOf(chars,start,end,'十')) == 0) {
                ret = 10+parse(chars, start + 1, end, 1);
            }else if ((index = indexOf(chars,start,end,'亿')) != -1) {
                ret = parse(chars, start,index, 1) * 100000000 + parse(chars,index + 1,end,10000000);
            } else if ((index = indexOf(chars,start,end,'万')) != -1) {
                ret = parse(chars, start,index, 1) * 10000 + parse(chars,index + 1,end,1000);
            } else if ((index = indexOf(chars,start,end,'千')) != -1) {
                ret = parse(chars, start, index, 1) * 1000 + parse(chars,index + 1,end,100);
            } else if ((index = indexOf(chars,start,end,'百')) != -1) {
                ret = parse(chars, start, index, 1) * 100 + parse(chars,index + 1,end,10);
            } else if ((index = indexOf(chars,start,end,'十')) != -1) {
                ret = parse(chars, start, index, 1) * 10 + parse(chars,index + 1,end,1);
            }else if ((index = indexOf(chars,start,end,'零')) != -1) {
                ret = parse(chars, start+1, index, 1) * 1 + parse(chars,index + 1,end,1);
            }

        }
        return ret;
    }
    private static int indexOf(char[] chars, int start, int end, char c) {
        for (int i = start; i < end; i++) {
            if (chars[i] == c)
                return i;
        }
        return -1;
    }
    /**
     * 将繁体中文转换为简体中文
     * @param chars
     */
    private static void changeTCtoSC(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            Character c = map.get(chars[i]);
            if (c != null)
                chars[i] = c;
        }
    }
    
    public static String parseDigits(int index)
    {
        char[] val = String.valueOf(index).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) 
        {
            String m = val[i] + "";
            int n = Integer.valueOf(m);
            boolean isZero = n == 0;
            String unit = units[(len - 1) - i];
            if (isZero) 
            {
                if ('0' == val[i - 1]) 
                {
                    continue;
                } 
                else 
                {
                    sb.append(SCDigits[n]);
                }
            } 
            else 
            {
                sb.append(SCDigits[n]);
                sb.append(unit);
            }
        }
        String ret = sb.toString();
        if (ret.endsWith("零"))
        {
            ret = ret.substring(0, ret.length() - 1);
        }
        if (index >= 10 && index < 20)
        {
            ret = ret.substring(1);
        }
        return ret;
    }
}