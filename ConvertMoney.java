/**
 * Created by dianaabbas on 3/13/14.
 * to run:  javac ConvertMoney.java
 *          java ConvertMoney {USD amount}
 * example: java ConvertMoney 2523.04
 */

import java.util.HashMap;

public class ConvertMoney {


    public static abstract class Converter {
        public abstract String convert(String money);
    }

    /*
        This class converts values between 1 and 19
     */
    public static class OnesConverter extends Converter {
        private final String[] ONES = new String[] {
            "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
        };

        @Override
        public String convert(String number) {
            StringBuffer buffer = new StringBuffer();
            int index;
            int value = Integer.parseInt(number);
            value %= 100;
            if(value < 10) index = value % 10 - 1;
            else index = value % 20 - 1;
            if(index < ONES.length) buffer.append(ONES[index]);
            return buffer.toString();
        }
    }

    /*
        Converts values between 20 and 99 inclusively
     */
    public static class TensConverter extends Converter {
        private final String[] TENS = new String[] {
            "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
        };

        private OnesConverter onesConverter = new OnesConverter();

        @Override
        public String convert(String number) {
            StringBuffer buffer = new StringBuffer();
            int index = -1;
            int value = Integer.parseInt(number);
            value %= 100;
            if(value >= 20) {
                index = value / 10 - 2;
                buffer.append(TENS[index]);
                value %= 10;
                if(value != 0) {
                    buffer.append("-");
                    String ones = onesConverter.convert(String.valueOf(value));
                    buffer.append(ones);
                }
            } else {
                value %= 20;
                String ones = onesConverter.convert(String.valueOf(value));
                if(value != 0) buffer.append(ones);
            }
            return buffer.toString();
        }
    }

    /*
        Converts values between 100 and 999 inclusively
     */
    public static class HundredsConverter extends Converter {
        private TensConverter tensConverter = new TensConverter();
        private OnesConverter onesConverter = new OnesConverter();

        @Override
        public String convert(String number) {
            StringBuffer buffer = new StringBuffer();
            int value = Integer.parseInt(number);
            value %= 1000;

            if(value >= 100) { //get digit for hundredths place
                buffer.append(onesConverter.convert(String.valueOf(value / 100)));
                buffer.append(" ");
                buffer.append("hundred");
                if(value%100 != 0) buffer.append(" ");
            }

            if((value%100) != 0) { // not evenly divisible by 100
                String tens = tensConverter.convert(String.valueOf(value%100));
                buffer.append(tens);
            }
           return buffer.toString();
        }
    }

    /*
        Recursively converts values greater than 100 and below 1,000,000,000,000.
        I did not include support for values greater than the billions.
        Support can be extended by adding words to static HashMap exponentWords
     */
    public static class CurrencyToTextConverter extends Converter {
        private HundredsConverter hundredsConverter = new HundredsConverter();
        private Converter lowConverter;
        private final int exponent;
        private static final HashMap<Integer, String> exponentWords = new HashMap<Integer, String>();
        static {
            exponentWords.put(3, "thousand");
            exponentWords.put(6, "million");
            exponentWords.put(9, "billion");
        }

        private int getExponent() {
            return exponent;
        }

        private String getExponentWord(int exponent) {
            return exponentWords.get(exponent);
        }

        //default constructor
        public CurrencyToTextConverter() {
            this(9);
        }

        //parameterized constructor
        public CurrencyToTextConverter(int exponent) {
            if(exponent <= 3) lowConverter = hundredsConverter; //base case
            else lowConverter = new CurrencyToTextConverter(exponent-3); //recursively splits value to convert by 3 exponential places
            this.exponent = exponent;
        }

        //convert cents on the right of decimal
        public String convertCents(String cents) {
            StringBuffer buffer = new StringBuffer();
            if(cents.matches("00")) return null;
            int value = Integer.parseInt(cents);
            buffer.append(" and ");
            if(value < 10) buffer.append("0");
            buffer.append(value);
            buffer.append("/100");
            return buffer.toString();
        }

        //converts a USD amount into a human readable string
        public String humanize(String amount) {
            if(!amount.matches("(\\d{1,12})\\.(\\d{2})")) return "Not in US currency format";
            String[] money = amount.split("\\.");
            StringBuffer buffer = new StringBuffer();
            String dollars = this.convert(money[0]);
            String cents = this.convertCents(money[1]);
            buffer.append(dollars);
            if(cents != null) buffer.append(cents);
            buffer.append(" dollars");
            return buffer.toString();
        }

        @Override
        public String convert(String number) {
            StringBuffer buffer = new StringBuffer();
            String loNumber, hiNumber, loText, hiText;
            loNumber = hiNumber = loText = hiText = null;

            if(number.length() <= getExponent()){ //base case for recursive call
                hiNumber = null;
                loNumber = number;
            } else { //recursive case
                int index = number.length() - getExponent();
                hiNumber = number.substring(0,index);
                loNumber = number.substring(index);
            }

            if(hiNumber != null) {
                 hiText = hundredsConverter.convert(hiNumber);
            }
            loText = lowConverter.convert(loNumber); //recursive call to CurrencyToTextConverter

            if(hiText != null) {
                buffer.append(hiText);
                buffer.append(" ");
                buffer.append(getExponentWord(getExponent()));
                if(loText != null) buffer.append(" ");
            }
            buffer.append(loText);

            return buffer.toString();
        }
    }

    public static void main(String[] args) {
        CurrencyToTextConverter converter = new CurrencyToTextConverter();
        String amount = args[0];
        String money = converter.humanize(amount);
        System.out.println(money);
        System.exit(0);
    }
}
