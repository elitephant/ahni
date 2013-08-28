package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import views.html.account.index;
import views.html.account.login;

@SecureSocial.SecuredAction
public class Account extends Controller{
    public static Result login() {
        return ok(login.render());
    }

    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user));
    }
}
