package controllers;

import controllers.*;
import models.User;
import models.UserDetail;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import services.InhaAuthenticator.EmailAuthenticator;
import services.InhaAuthenticator.PortalAuthenticator;
import services.InhaAuthenticator.models.Email;
import services.InhaAuthenticator.models.Portal;
import views.html.account.index;
import views.html.account.detail;
import views.html.account.login;
import views.html.account.startAuthenticate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SecureSocial.SecuredAction
public class Account extends Controller{
    static Form<Email> emailAuthenticateForm = Form.form(Email.class);
    static Form<Portal> portalAuthenticateForm = Form.form(Portal.class);
    static Form<UserDetail> userDetailForm = Form.form(UserDetail.class);

    public static Result login() {
        return ok(login.render());
    }

    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User dbUser = User.findByIdentity(user);
        UserDetail userDetail = UserDetail.findByUserId(dbUser.id);
        return ok(index.render(user, userDetail));
    }

    public static Result detail() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        User dbUser = User.findByIdentity(user);
        UserDetail userDetail = UserDetail.findByUserId(dbUser.id);
        if (userDetail == null) {
            flash().put("error","사용자 정보를 찾을 수 없습니다.");
            return redirect(controllers.routes.Account.index());
        }
        else {
            Form<UserDetail> filledForm = userDetailForm.fill(userDetail);
            return ok(detail.render(user, filledForm));
        }
    }

    public static Result updateUserDetail() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Form<UserDetail> boundForm = userDetailForm.bindFromRequest();
        if(boundForm.hasErrors()) {
            return badRequest(detail.render(user, boundForm));
        }
        UserDetail userDetail = boundForm.get();

        //사용자 정보 업데이트
        if(userDetail.update(user)) {
            flash().put("success","사용자 정보를 업데이트 하였습니다.");
            return redirect(controllers.routes.Account.index());
        }
        else {
            flash().put("error","업데이트에 실패하였습니다.");
            return redirect(controllers.routes.Account.index());
        }
    }

    /**
     * 인하대학생 인증 시작 화면 렌더링
     * @return
     */
    public static Result startAuthenticate() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User dbUser = User.findByIdentity(user);
        UserDetail userDetail = UserDetail.findByUserId(dbUser.id);

        if(UserDetail.isValidatedUser(userDetail)) {
            flash("error", "이미 인하대학생 인증을 하였습니다.");
            return redirect(controllers.routes.Account.index());
        }
        else {
            return ok(startAuthenticate.render(emailAuthenticateForm, portalAuthenticateForm));
        }
    }

    /**
     * 인하대 이메일 인증
     * @return
     */
    public static Result requestEmailAuthenticate() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User dbUser = User.findByIdentity(user);

        Form<Email> boundForm = emailAuthenticateForm.bindFromRequest();

        if(boundForm.hasErrors()) {
            return redirect(controllers.routes.Account.startAuthenticate());
        }

        String email = boundForm.get().email;

        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);

        if (m.find()) {
            flash("error", "아이디에 특수 문자를 포함할 수 없습니다.");
            return redirect(controllers.routes.Account.startAuthenticate());
        }

        EmailAuthenticator.sendEmail(email, dbUser.id, ctx()._requestHeader());
        flash("success", String.format("%s@inha.edu 주소로 인증 메일을 발송하였습니다.", email));

        return redirect(controllers.routes.Account.startAuthenticate());
    }

    /**
     * 인하대 포털 인증
     * @return
     */
    public static Result requestPortalAuthenticate() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User dbUser = User.findByIdentity(user);

        Form<Portal> boundForm = portalAuthenticateForm.bindFromRequest();

        if(boundForm.hasErrors()) {
            return redirect(controllers.routes.Account.startAuthenticate());
        }

        Portal portal = boundForm.get();

        PortalAuthenticator portalAuthenticator = new PortalAuthenticator();
        boolean result = portalAuthenticator.doAuthenticate(portal.uid, portal.pwd);

        if(result) {
            EmailAuthenticator.addUserToValidatedList(dbUser.id);
            flash("success", "인하대학교 학생 인증에 성공하였습니다.");
            return redirect(controllers.routes.Account.detail());
        } else {
            flash("error","인하대학교 학생 인증에 실패하였습니다.");
            return redirect(controllers.routes.Account.startAuthenticate());
        }
    }

    /**
     * 인하대 이메일 인증 완료
     * @param token
     * @return
     */
    public static Result completeAuthenticate(String token) {
        if(EmailAuthenticator.validateToken(token)) {
            flash("success","인하대학교 학생 인증에 성공하였습니다.");
            return redirect(controllers.routes.Account.detail());
        }
        else {
            flash("error","인하대학교 학생 인증에 실패하였습니다.");
            return redirect(controllers.routes.Account.index());
        }
    }
}
