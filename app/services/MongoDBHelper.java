package services;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.joda.time.DateTime;
import play.Play;
import scala.Some;
import securesocial.core.*;
import securesocial.core.java.Token;

import java.net.UnknownHostException;

public class MongoDBHelper {

    private static DB db = getConnection();

    private static DB getConnection(){
        String database = Play.application().configuration().getString("mongodb.database");
        String credentials = Play.application().configuration().getString("mongodb.credentials");
        String servers = Play.application().configuration().getString("mongodb.servers");

        DB db;
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(servers.split(":")[0],Integer.parseInt(servers.split(":")[1]));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        db = mongoClient.getDB(database);
        db.authenticate(credentials.split(":")[0],credentials.split(":")[1].toCharArray());

        return db;
    }

    /**
     * DB 인스턴스 리턴
     * @return DB
     */
    public static DB getDB() {
        return db;
    }

    /**
     * Identity -> DBObject, 유저 정보를 MongoDB에 저장할 수 있도록 변환.
     * @param user - 유저 정보
     * @return DBObject - MongoDB 객체
     */
    public static DBObject identityToDoc(Identity user){
        BasicDBObject doc = new BasicDBObject("UserIdAndProviderId", user.identityId().userId() + user.identityId().providerId())
                .append("authMethod", new BasicDBObject("method", user.authMethod().method()))
                .append("firstName", user.firstName())
                .append("fullName", user.fullName())
                .append("lastName", user.lastName());

        if(user.identityId()!=null) {
            doc.append("identityId", new BasicDBObject("productPrefix",user.identityId().productPrefix())
                    .append("providerId",user.identityId().providerId())
                    .append("userId",user.identityId().userId()));
        }
        if(user.email().nonEmpty()){
            doc.append("email", user.email().get());
        }
        if(user.avatarUrl().nonEmpty()){
            doc.append("avatarUrl", user.avatarUrl().get());
        }
        if(user.oAuth1Info().nonEmpty() && user.oAuth1Info() != null && user.oAuth1Info().get() != null)  {
            doc.append("oAuth1Info", new BasicDBObject("secret", user.oAuth1Info().get().secret())
                    .append("token",user.oAuth1Info().get().token()));
        }
        if(user.oAuth2Info().nonEmpty() && user.oAuth2Info() != null && user.oAuth2Info().get() != null) {
            BasicDBObject oAuth2Info = new BasicDBObject("accessToken", user.oAuth2Info().get().accessToken());
            if(user.oAuth2Info().get().tokenType().nonEmpty()){
                oAuth2Info.append("tokenType",user.oAuth2Info().get().tokenType().get());
            }
            if(user.oAuth2Info().get().refreshToken().nonEmpty()){
                oAuth2Info.append("refreshToken",user.oAuth2Info().get().refreshToken());
            }
            if(user.oAuth2Info().get().expiresIn().nonEmpty()){
                oAuth2Info.append("expiresIn",user.oAuth2Info().get().expiresIn().get());
            }
            doc.append("oAuth2Info", oAuth2Info);
        }

        if(user.passwordInfo().nonEmpty() && user.passwordInfo() != null && user.passwordInfo().get() != null) {
            BasicDBObject passwordInfo = new BasicDBObject("password", user.passwordInfo().get().password())
                    .append("hasher",user.passwordInfo().get().hasher());
            if(user.passwordInfo().get().salt().nonEmpty()){
                passwordInfo.append("salt",user.passwordInfo().get().salt().get());
            }
            doc.append("passwordInfo", passwordInfo);
        }

        return doc;
    }

    public static DBObject tokenToDoc(Token token){
        BasicDBObject doc = new BasicDBObject("uuid", token.getUuid())
                .append("token", new BasicDBObject("email",token.getEmail())
                        .append("creationTime",token.getCreationTime().toDate())
                        .append("expirationTime",token.getExpirationTime().toDate())
                        .append("isSignUp",token.getIsSignUp()));

        return  doc;
    }

    public static DBObject tokenToDoc(Token token, String userId){
        BasicDBObject doc = new BasicDBObject("uuid", token.getUuid())
                .append("userId",userId)
                .append("token", new BasicDBObject("email",token.getEmail())
                        .append("creationTime",token.getCreationTime().toDate())
                        .append("expirationTime",token.getExpirationTime().toDate())
                        .append("isSignUp",token.getIsSignUp()));

        return  doc;
    }

    /**
     * DBObject -> Identity, 데이터베이스에 있는 유저 정보를 Identity 타입에 맞춰서 가져옴.
     * @param doc MongoDB - JSON 형태로 저장되어 있는 유저 정보
     * @return Identity - securesocial에서 사용할 수 있는 유저 객체
     */
    public static Identity docToIdentity(DBObject doc){
        Identity identity=null;
        if(doc!=null){
            DBObject identityId = (DBObject)doc.get("identityId");
            DBObject authMethod = (DBObject)doc.get("authMethod");
            DBObject oAuth1Info = (DBObject)doc.get("oAuth1Info");
            DBObject oAuth2Info = (DBObject)doc.get("oAuth2Info");
            DBObject passwordInfo = (DBObject)doc.get("passwordInfo");

            AuthenticationMethod method = null;
            if(String.valueOf(authMethod.get("method")).equals("oauth1")){method = AuthenticationMethod.OAuth1();}
            else if(String.valueOf(authMethod.get("method")).equals("oauth2")){method = AuthenticationMethod.OAuth2();}
            else if(String.valueOf(authMethod.get("method")).equals("userPassword")){method = AuthenticationMethod.UserPassword();}
            else {method = AuthenticationMethod.OpenId();}

            OAuth1Info oa1 = null;
            if(oAuth1Info!=null) { oa1 = new OAuth1Info(String.valueOf(oAuth1Info.get("secret")),String.valueOf(oAuth1Info.get("token")));}

            OAuth2Info oa2 = null;
            if(oAuth2Info!=null) {
                String tokenType = "";
                Object expiresIn = null;
                String refreshToken = "";

                if(oAuth2Info.get("tokenType")!=null) {tokenType = String.valueOf(oAuth2Info.get("tokenType"));}
                if(oAuth2Info.get("expiresIn")!=null) {expiresIn = oAuth2Info.get("expiresIn");}
                if(oAuth2Info.get("refreshToken")!=null) {refreshToken = String.valueOf(oAuth2Info.get("refreshToken"));}
                oa2 = new OAuth2Info(String.valueOf(oAuth2Info.get("accessToken")),new Some<String>(tokenType),new Some<Object>(expiresIn),new Some<String>(refreshToken));
            }

            PasswordInfo pinfo = null;
            if(passwordInfo!=null) {
                String hasher = "";
                String password = "";
                String salt = "";

                if(passwordInfo.get("hasher")!=null) {hasher = String.valueOf(passwordInfo.get("hasher"));}
                if(passwordInfo.get("password")!=null) {password = String.valueOf(passwordInfo.get("password"));}
                if(passwordInfo.get("salt")!=null) {salt = String.valueOf(passwordInfo.get("salt"));}
                pinfo = new PasswordInfo(hasher,password,new Some<String>(salt));
            }

            identity = new SocialUser(new IdentityId(String.valueOf(identityId.get("userId")),String.valueOf(identityId.get("providerId"))),
                    String.valueOf(doc.get("firstName")),
                    String.valueOf(doc.get("lastName")),
                    String.valueOf(doc.get("fullName")),
                    new Some<String>(String.valueOf(doc.get("email"))),
                    new Some<String>(String.valueOf(doc.get("avatarUrl"))),
                    method,
                    new Some<OAuth1Info>(oa1),
                    new Some<OAuth2Info>(oa2),
                    new Some<PasswordInfo>(pinfo)
            );
        }

        return identity;
    }

    public static Token docToToken(DBObject doc) {
        Token token = new Token();

        if(doc!=null){
            DBObject tokenObject = (DBObject)doc.get("token");
            token.setUuid(String.valueOf(doc.get("uuid")));

            if(tokenObject!=null){
                token.setEmail(String.valueOf(tokenObject.get("email")));
//                token.setCreationTime(DateTime.parse(String.valueOf(tokenObject.get("creationTime"))));
//                token.setExpirationTime(DateTime.parse(String.valueOf(tokenObject.get("expirationTime"))));
                token.setCreationTime(new DateTime(tokenObject.get("creationTime")));
                token.setExpirationTime(new DateTime(tokenObject.get("expirationTime")));
                token.setIsSignUp(Boolean.valueOf(String.valueOf(tokenObject.get("isSignUp"))));
            }
        }

        return token;
    }
}
