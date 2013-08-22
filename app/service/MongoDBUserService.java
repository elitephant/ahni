package service;

import com.mongodb.DBObject;
import scala.Some;
import securesocial.core.*;

public class MongoDBUserService  {
    public static Identity docToIdentity(DBObject doc){
        Identity iden=null;
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
                if(oAuth2Info.get("password")!=null) {password = String.valueOf(passwordInfo.get("password"));}
                if(oAuth2Info.get("salt")!=null) {salt = String.valueOf(passwordInfo.get("salt"));}
                pinfo = new PasswordInfo(hasher,password,new Some<String>(salt));
            }

            iden = new SocialUser(new IdentityId(String.valueOf(identityId.get("userId")),String.valueOf(identityId.get("providerId"))),
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

        return iden;
    }

}
