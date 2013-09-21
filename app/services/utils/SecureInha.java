package services.utils;

import controllers.routes;
import models.User;
import models.UserDetail;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

public class SecureInha extends Security.Authenticator {
    public SecureInha() {
        super();
    }

    @Override
    public String getUsername(Http.Context context) {
        Identity user = (Identity) context.args.get(SecureSocial.USER_KEY);
        final String SESSION_KEY =
                String.format("%s.%s.inha.email", user.identityId().userId(), user.identityId().providerId());

        String sessionInhaMali = "";
        if(context._requestHeader().session().get(SESSION_KEY).nonEmpty()) {
            sessionInhaMali = context._requestHeader().session().get(SESSION_KEY).get();
        }

        //세선에 인하 이메일이 있다면
        if(sessionInhaMali.endsWith("@inha.edu"))
        {
            return sessionInhaMali;
        }
        //세션에 인하 이메일이 없다면
        else {
            User dbUser = User.findByIdentity(user);
            UserDetail userDetail = UserDetail.findByUserId(dbUser.id);

            //디비에 UserDetail에 인하 메일이 있는 유저라면
            if(userDetail != null && userDetail.validatedEmail != "") {
                context.session().put(SESSION_KEY, userDetail.validatedEmail);
                return userDetail.validatedEmail;
            }
            else {
                return null;
            }
        }
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        //인하 메일을 인증하지 않은 유저라면 인증 페이지로 redirect
        context.flash().put("error","인하대학생 인증이 필요한 기능입니다");
        return redirect(routes.Account.startAuthenticate());
    }
}