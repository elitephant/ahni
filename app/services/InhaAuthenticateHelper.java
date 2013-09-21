package services;

import com.mongodb.*;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import controllers.routes;
import models.UserDetail;
import org.joda.time.DateTime;
import play.api.mvc.RequestHeader;
import securesocial.core.java.Token;

import java.util.UUID;

public class InhaAuthenticateHelper {

    /**
     * 파라미터로 받은 userid 를 이용해 emailId@inha.edu 계정으로 인증 토큰을 보낸다.
     * @param emailId 교내 이메일 아이디
     * @param userId 몽고 DB Users 컬렉션의 id
     * @param requestHeader
     */
    public static void sendEmail(String emailId, String userId, RequestHeader requestHeader){
        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();

        String uuid = UUID.randomUUID().toString();

        mail.setSubject("[ahni] 인하대학교 학생 인증 메일입니다");
        mail.addRecipient(String.format("%s@inha.edu",emailId));
        mail.addFrom("rmrhtdms@gmail.com");
        mail.send(routes.Account.completeAuthenticate(uuid).absoluteURL(false, requestHeader));

        //토큰 객체 생성
        Token token = new Token();
        DateTime now = DateTime.now();
        token.setUuid(uuid);
        token.setCreationTime(now);
        token.setExpirationTime(now.plusMinutes(60));
        token.setIsSignUp(false);
        token.setEmail(String.format("%s@inha.edu",emailId));

        //디비에 토큰을 넣음
        DBCollection coll = MongoDBHelper.getDB().getCollection("Tokens");
        coll.insert(MongoDBHelper.tokenToDoc(token, userId), WriteConcern.SAFE);
    }

    /**
     * 파라미터로 받은 uuid 가 유효한 토큰인지 인증한다.
     * @param uuid
     * @return
     */
    public static boolean validateToken(String uuid) {
        DBCollection coll = MongoDBHelper.getDB().getCollection("Tokens");
        DBObject obj = coll.findOne(new BasicDBObject("uuid",uuid));
        if(obj!=null){
            addUserToValidatedList(obj);

            DBObject token = (DBObject)obj.get("token");
            String email = String.valueOf(token.get("email"));

            BasicDBObject query = new BasicDBObject("token.email",email);
            DBCursor cur = coll.find(query);
            for(DBObject toRemove : cur) {
                coll.remove(toRemove);
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 해당 유저와 이메일을 인증 목록에 추가한다.
     * @param obj
     */
    public static void addUserToValidatedList(DBObject obj) {
        DBObject token = (DBObject)obj.get("token");
        DBCollection coll = MongoDBHelper.getDB().getCollection("UserDetails");
        DBObject userDetail = new BasicDBObject("userId", String.valueOf(obj.get("userId")))
                .append("validatedTime",DateTime.now().toDate())
                .append("validatedEmail", String.valueOf(token.get("email")));

        coll.insert(userDetail, WriteConcern.SAFE);
    }

    public static boolean isValidatedUser(UserDetail userDetail) {
        if(userDetail != null) {
            if(userDetail.validatedEmail.contains("@inha.edu")) {
                return true;
            }
        }
        return false;
    }
}
