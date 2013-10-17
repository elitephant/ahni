package services.InhaAuthenticator;

import org.apache.commons.codec.binary.Base64;
import play.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class PortalAuthenticator {
    private static final String REQUEST_FORM_DATA = "dest=http%3A%2F%2Fwww.inha.ac.kr&uid={uid}&pwd={pwd}";
    private static final String LOGIN_URL = "https://www.inha.ac.kr/common/asp/login/loginProcess.asp";
    private static final String LOGIN_SUCCESS_MESSAGE = "로그인 되었습니다";

    public boolean doAuthenticate(String uid, String pwd) {
        String _uid = new String(Base64.encodeBase64(uid.getBytes()));
        String _pwd = new String(Base64.encodeBase64(pwd.getBytes()));
        return getHtml(_uid, _pwd);
    }

    private boolean getHtml(String uid, String pwd) {
        boolean result = false;

        HttpURLConnection hConnection = null;
        PrintStream ps = null;
        InputStream is = null;
        BufferedReader in = null;

        try{
            URL url = new URL(LOGIN_URL);
            hConnection = (HttpURLConnection)url.openConnection();
            hConnection.setInstanceFollowRedirects(false);
            hConnection.setDoOutput(true);
            hConnection.setRequestMethod("POST");
            ps = new PrintStream(hConnection.getOutputStream());

            //인코딩된 uid, pwd 로 문자열 replace
            ps.print(REQUEST_FORM_DATA.replace("{uid}", uid).replace("{pwd}", pwd));

            if((is = hConnection.getInputStream()) != null)
            {
                in = new BufferedReader(new InputStreamReader(is, Charset.forName("EUC-KR")));
                String readLine;

                while((readLine=in.readLine()) != null)
                {
                    if(readLine.contains(LOGIN_SUCCESS_MESSAGE)) {
                        result = true;
                        Logger.debug("인하대 포털 인증 - Success");
                        break;
                    }
                }
            }
            else {
                Logger.error("인하대 포털 인증 - Fail");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            //스트림 닫기
            hConnection.disconnect();
            ps.close();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return result;
    }
}