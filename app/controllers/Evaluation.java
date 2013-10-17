package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
import models.LectureSimple;
import models.LectureEvaluation;
import models.User;
import play.data.*;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import services.InhaAuthenticator.SecureInha;
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

        LectureSimple lectureSimple = LectureSimple.findById(id);

        if(lectureSimple==null) {
            flash("error", "해당 페이지를 찾을 수 없습니다 :(");
            return redirect(controllers.routes.Evaluation.index());
        }
        else {
            return ok(detail.render(user, LectureSimple.findById(id)));
        }
    }

    /**
     * 강의평가 작성하는 화면 렌더링
     * @param lecture 작성할 강의의 몽고 아이디
     * @return
     */
    public static Result write(String lecture) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User dbUser = User.findByIdentity(user);
        LectureSimple lectureSimple = LectureSimple.findById(lecture);

        if(lectureSimple.evaluations != null) {
            for(LectureEvaluation lectureEvaluation : lectureSimple.evaluations) {
                if(lectureEvaluation.user.equals(dbUser.id)) {
                    Form<LectureEvaluation> filledForm = lectureEvaluationForm.fill(lectureEvaluation);
                    return ok(write.render(user, lectureSimple, filledForm, true));
                }
            }
        }

        return ok(write.render(user, lectureSimple, lectureEvaluationForm, false));
    }

    /**
     * 강의평가 저장하는 액션
     * @return
     */
    public static Result save(String id, Boolean isEdit) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Form<LectureEvaluation> filledForm = lectureEvaluationForm.bindFromRequest();

        if(filledForm.hasErrors()) {
            return badRequest(index.render(user, searchForm, LectureSimple.all()));
        } else {
            //성공적으로 강의평가 저장
            if(LectureSimple.addEvaluation(filledForm.get(), user, id, isEdit)) {
                return redirect(controllers.routes.Evaluation.detail(id));
            } else {
                return badRequest(index.render(user, searchForm, LectureSimple.all()));
            }
        }
    }

    public static Result delete(String id) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User dbUser = User.findByIdentity(user);

        BasicDBObject findQuery = new BasicDBObject("_id", new org.bson.types.ObjectId(id))
                .append("evaluations.user",new org.bson.types.ObjectId(dbUser.id));

        BasicDBObject pullQuery = new BasicDBObject("$pull",
                new BasicDBObject("evaluations",
                        new BasicDBObject("user",
                                new org.bson.types.ObjectId(dbUser.id)))
        );

        LectureSimple.coll.setWriteConcern(WriteConcern.SAFE);
        LectureSimple.coll.update(findQuery, pullQuery);

        return redirect(controllers.routes.Account.index());
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

        return redirect(controllers.routes.Evaluation.index());
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
