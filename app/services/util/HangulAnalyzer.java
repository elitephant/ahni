package services.util;

public class HangulAnalyzer {
    /**
     * 파라미터로 받은 한글 original 마지막 문자의 받침(종성) 유무에 따라 suffix1 또는 suffix2 를 반환
     * @param original
     * @param suffix1
     * @param suffix2
     * @return 받침이 있으면 original+suffix1, 받침이 없으면 original+suffix2
     */
    public static String getSuffix(String original, String suffix1, String suffix2) {
        try {
            char comVal = (char) (original.charAt(original.length()-1)-0xAC00);

            if (comVal >= 0 && comVal <= 11172){
                char uniVal = comVal;

                char jong = (char) ((uniVal % 28) + 0x11a7);

                //받침이 있다
                if(jong!=4519){
                    return original.concat(suffix1);
                }
                //받침이 없다
                else {
                    return original.concat(suffix2);
                }
            } else {
                // 한글이 아닐경우
                return original.concat(suffix1);
            }
        } catch (Exception e) {
            return "";
        }
    }
}
