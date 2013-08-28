package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import views.html.home.index;
import views.html.home.review;

@SecureSocial.SecuredAction
public class Home extends Controller {
    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user));
    }

    public static Result review() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(review.render(user));
    }
}
