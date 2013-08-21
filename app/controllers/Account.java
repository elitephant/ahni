package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.login;

public class Account extends Controller{
    public static Result login() {
        return ok(login.render());
    }
}
