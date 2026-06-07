package java1.casting;

public class Casting3 {

    public static void main(String[] args) {
        long maxIntValue = 2147483647; //int 최고값
        long maxIntOver = 2147483648L; //int 최고값 + 1(초과)
        int intValue = 0;

        intValue = (int) maxIntValue; //형변환
        System.out.println("maxIntValue java1.casting=" + intValue);

        intValue = (int) maxIntOver; //형변환
        System.out.println("maxIntOver java1.casting=" + intValue);
    }
}
