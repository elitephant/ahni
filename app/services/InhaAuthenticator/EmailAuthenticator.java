package services.InhaAuthenticator;

import com.mongodb.*;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import controllers.routes;
import models.UserDetail;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import play.Logger;
import play.api.mvc.RequestHeader;
import securesocial.core.IdentityProvider;
import securesocial.core.java.Token;
import services.MongoDBHelper;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailAuthenticator {
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
        mail.addFrom("ahni <rmrhtdms@gmail.com>");
        mail.sendHtml(
                "<p>아래의 링크를 누르면 인증이 완료됩니다.</p>"
                        +"<p>"+routes.Account.completeAuthenticate(uuid).absoluteURL(IdentityProvider.sslEnabled(), requestHeader)+"</p>"
        );

        //토큰 객체 생성
        Token token = new Token();
        DateTime now = DateTime.now();
        token.setUuid(uuid);
        token.setCreationTime(now);
        token.setExpirationTime(now.plusMinutes(60));
        token.setIsSignUp(false);
        token.setEmail(String.format("%s@inha.edu",emailId));

        //디비에 토큰을 넣음
        DBCollection coll = MongoDBHelper.getDB().getCollection("token");
        coll.insert(MongoDBHelper.tokenToDoc(token, userId), WriteConcern.SAFE);
    }

    /**
     * 파라미터로 받은 uuid 가 유효한 토큰인지 인증한다.
     * @param uuid
     * @return
     */
    public static boolean validateToken(String uuid) {
        //uuid 포맷 검증. DB 검색을 피하기 위함.
        try {
            UUID validateUUID = UUID.fromString(uuid);
            if(!uuid.equals(validateUUID.toString())) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }

        DBCollection coll = MongoDBHelper.getDB().getCollection("token");
        DBObject obj = coll.findOne(new BasicDBObject("uuid",uuid));
        if(obj!=null){
            addUserToValidatedList(String.valueOf(obj.get("userId")));

            DBObject token = (DBObject)obj.get("token");
            String email = String.valueOf(token.get("email"));

            //해당 이메일로 발행된 토큰 검색
            BasicDBObject query = new BasicDBObject("token.email",email);
            DBCursor cur = coll.find(query);

            //해당 이메일로 발행된 토큰 모두 삭제
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
     * @param user
     */
    public static void addUserToValidatedList(String user) {
        UserDetail toInsert = new UserDetail();
        toInsert.isValidated = true;
        toInsert.validatedTime = DateTime.now().toDate();
        toInsert.user = user;

        UserDetail.coll.insert(toInsert);
    }
}