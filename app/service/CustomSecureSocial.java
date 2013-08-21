package service;//package service;
//
//import play.api.data.Form;
//import play.api.mvc.Request;
//import play.api.mvc.RequestHeader;
//import play.api.templates.Html;
//import play.api.templates.Txt;
//import scala.None;
//import scala.Option;
//import scala.Tuple2;
//import securesocial.controllers.PasswordChange;
//import securesocial.controllers.Registration;
//import securesocial.controllers.TemplatesPlugin;
//import securesocial.core.Identity;
//import securesocial.core.SecuredRequest;
//
//public class CustomSecureSocial implements TemplatesPlugin {
//    @Override
//    public void $init$() {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onStart() {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onStop() {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public boolean enabled() {
//        return false;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public <A> Html getLoginPage(Request<A> request, Form<Tuple2<String, String>> form, Option<String> msg) {
//        return views.html.custom.login.render(request, form, msg);
//    }
//
//    @Override
//    public <A> Html getSignUpPage(Request<A> request, Form<Registration.RegistrationInfo> form, String token) {
//        return views.html.custom.Registration.signUp.render(request, form, token);
//    }
//
//    @Override
//    public <A> Html getStartSignUpPage(Request<A> request, Form<String> form) {
//        return views.html.custom.Registration.startSignUp.render(request, form);
//    }
//
//    @Override
//    public <A> Html getResetPasswordPage(Request<A> request, Form<Tuple2<String, String>> form, String token) {
//        return views.html.custom.Registration.resetPasswordPage.render(request, form, token);
//    }
//
//    @Override
//    public <A> Html getStartResetPasswordPage(Request<A> request, Form<String> form) {
//        return views.html.custom.Registration.startResetPassword.render(request, form);
//    }
//
//    @Override
//    public <A> Html getPasswordChangePage(SecuredRequest<A> request, Form<PasswordChange.ChangeInfo> form) {
//        return views.html.custom.passwordChange.render(request,form);
//    }
//
//    @Override
//    public <A> Html getNotAuthorizedPage(Request<A> request) {
//        return views.html.custom.notAuthorized.render();
//    }
//
//    @Override
//    public Tuple2<Option<Txt>, Option<Html>> getSignUpEmail(String token, RequestHeader request) {
//        return Tuple2.apply(None, Option.apply(views.html.custom.mails.signUpEmail.render(request,token)));
//    }
//
//    @Override
//    public Tuple2<Option<Txt>, Option<Html>> getAlreadyRegisteredEmail(Identity user, RequestHeader request) {
//        return Tuple2.apply(None, Option.apply(views.html.custom.mails.alreadyRegisteredEmail.render(request, user)));
//    }
//
//    @Override
//    public Tuple2<Option<Txt>, Option<Html>> getWelcomeEmail(Identity user, RequestHeader request) {
//        return Tuple2.apply(None, Option.apply(views.html.custom.mails.welcomeEmail.render(request, user)));
//    }
//
//    @Override
//    public Tuple2<Option<Txt>, Option<Html>> getUnknownEmailNotice(RequestHeader request) {
//        return Tuple2.apply(None, Option.apply(views.html.custom.mails.unknownEmailNotice.render(request)));
//    }
//
//    @Override
//    public Tuple2<Option<Txt>, Option<Html>> getSendPasswordResetEmail(Identity user, String token, RequestHeader request) {
//        return Tuple2.apply(None, Option.apply(views.html.custom.mails.passwordResetEmail.render(request, user, token)));
//    }
//
//    @Override
//    public Tuple2<Option<Txt>, Option<Html>> getPasswordChangedNoticeEmail(Identity user, RequestHeader request) {
//        return Tuple2.apply(None, Option.apply(views.html.custom.mails.passwordChangedNotice.render(request, user)));
//    }
//}
