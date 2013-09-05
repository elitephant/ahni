package controllers;

import models.Lecture;
import models.LectureEvaluation;
import play.data.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import views.html.evaluation.index;

@SecureSocial.SecuredAction
public class Evaluation extends Controller {
    static Form<LectureEvaluation> lectureEvaluationForm = Form.form(LectureEvaluation.class);
    static Form<String> searchForm = Form.form(String.class);

    /**
     * 인덱스 액션
     * @return
     */
    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user, lectureEvaluationForm, searchForm, LectureEvaluation.all()));
    }

    /**
     * 강의평가 저장하는 액션
     * @return
     */
    public static Result save() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Form<LectureEvaluation> filledForm = lectureEvaluationForm.bindFromRequest();

        if(filledForm.hasErrors()) {
            return badRequest(index.render(user, filledForm, searchForm, LectureEvaluation.all()));
        } else {
            LectureEvaluation.create(filledForm.get(), user);
            return redirect(routes.Evaluation.index());
        }
    }

    /**
     * 키워드 검색 액션
     * @return
     */
    public static Result search() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Form<String> filledForm = searchForm.bindFromRequest();

        for (String keyword : filledForm.data().values()) {
            return ok(index.render(user, lectureEvaluationForm, searchForm, LectureEvaluation.findByKeyword(keyword)));
        }

        return redirect(routes.Evaluation.index());
    }

    public static Result getLectureNames(String term) {
        return ok(Json.toJson(Lecture.find(term)));
    }
}
