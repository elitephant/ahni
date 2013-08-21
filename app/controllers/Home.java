package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import views.html.home.index;

public class Home extends Controller {
    @SecureSocial.SecuredAction
    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user));
    }
}
