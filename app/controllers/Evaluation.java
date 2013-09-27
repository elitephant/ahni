package controllers;

import models.LectureSimple;
import models.LectureEvaluation;
import play.data.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import services.utils.SecureInha;
import views.html.evaluation.index;
import views.html.evaluation.write;
import views.html.evaluation.detail;

@SecureSocial.SecuredAction
@Security.Authenticated(SecureInha.class)
public class Evaluation extends Controller {
    static Form<LectureEvaluation> lectureEvaluationForm = Form.form(LectureEvaluation.class);
    static Form<String> searchForm = Form.form(String.class);

    /**
     * 인덱스 액션
     * @return
     */
    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user, searchForm, LectureSimple.all()));
    }

    public static Result detail(String id) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        return ok(detail.render(user, LectureSimple.findById(id)));
    }

    /**
     * 강의평가 작성하는 화면 렌더링
     * @param lecture 작성할 강의의 몽고 아이디
     * @return
     */
    public static Result write(String lecture) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LectureSimple lectureSimple = LectureSimple.findById(lecture);
        return ok(write.render(user, lectureSimple, lectureEvaluationForm));
    }

    /**
     * 강의평가 저장하는 액션
     * @return
     */
    public static Result save(String id) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Form<LectureEvaluation> filledForm = lectureEvaluationForm.bindFromRequest();

        if(filledForm.hasErrors()) {
            return badRequest(index.render(user, searchForm, LectureSimple.all()));
        } else {
            LectureSimple.addEvaluation(filledForm.get(), user, id);
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
            return ok(index.render(user, searchForm, LectureSimple.findByKeyword(keyword)));
        }

        return redirect(routes.Evaluation.index());
    }

    /**
     * 강의평가 Form 에서 ajax 로 호출되는 액션. DB 에서 강의명을 읽어와 json 으로 출력.
     * @param term
     * @return
     */
    public static Result getLectureNames(String term) {
        return ok(Json.toJson(LectureSimple.findLectureNameByKeyword(term)));
    }

    /**
     * 강의평가 Form 에서 ajax 로 호출되는 액션. DB 에서 교수명을 읽어와 json 으로 출력.
     * @param term
     * @return
     */
    public static Result getProfessorNames(String term) {
        return ok(Json.toJson(LectureSimple.findProfessorNameByKeyword(term)));
    }
}
