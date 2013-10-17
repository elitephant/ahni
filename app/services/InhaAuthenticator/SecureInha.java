package services.InhaAuthenticator;

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
        final String SESSION_KEY = String.format("in%sha", user.identityId().userId());

        String session = "";
        if(context._requestHeader().session().get(SESSION_KEY).nonEmpty()) {
            session = context._requestHeader().session().get(SESSION_KEY).get();
        }

        //세션에 인증이 기록 되었다면
        if(session != null && session.equals("true"))
        {
            return "true";
        }
        //세션에 인증이 기록 되지 않았다면
        else {
            User dbUser = User.findByIdentity(user);
            UserDetail userDetail = UserDetail.findByUserId(dbUser.id);

            //인증된 유저라면
            if(userDetail != null && UserDetail.isValidatedUser(userDetail)) {
                context.session().put(SESSION_KEY, "true");
                return "true";
            }
            //인증되지 않은 유저라면
            else {
                return null;
            }
        }
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        context.flash().put("error","인하대학생 인증이 필요한 기능입니다");
        return redirect(routes.Account.startAuthenticate());
    }
}