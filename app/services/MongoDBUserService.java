package services;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import play.Application;
import scala.Option;
import securesocial.core.Identity;
import securesocial.core.IdentityId;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MongoDBUserService extends BaseUserService {
    public MongoDBUserService(Application application) {
        super(application);
    }

    /**
     * users 컬렉션에 몽고 오브젝트로 변환된 user를 넣는다.
     * @param user
     * @return user
     */
    @Override
    public Identity doSave(Identity user) {
        DBCollection coll = MongoDBHelper.getDB().getCollection("user");
        DBObject userObject = coll.findOne(new BasicDBObject("UserIdAndProviderId",user.identityId().userId() + user.identityId().providerId()));

        //디비에 없으면 insert
        if(userObject==null) { coll.insert(MongoDBHelper.identityToDoc(user)); }
        //디비에 있으면 update
        else { coll.update(userObject, MongoDBHelper.identityToDoc(user)); }

        return user;
    }

    /**
     * tokens 컬렉션에 몽고 오브젝트로 변환된 token을 넣는다.
     * @param token
     */
    @Override
    public void doSave(Token token) {
        DBCollection coll = MongoDBHelper.getDB().getCollection("token");
        coll.insert(MongoDBHelper.tokenToDoc(token));
    }

    @Override
    public Identity doFind(IdentityId userId) {
        DBCollection coll = MongoDBHelper.getDB().getCollection("user");
        DBObject obj = coll.findOne(new BasicDBObject("UserIdAndProviderId",userId.userId() + userId.providerId()));

        Identity user = MongoDBHelper.docToIdentity(obj);

        return user;
    }

    @Override
    public Token doFindToken(String tokenId) {
        DBCollection coll = MongoDBHelper.getDB().getCollection("token");
        DBObject obj = coll.findOne(new BasicDBObject("uuid", tokenId));
        if(obj==null) return null;

        //TODO: DB에 들어가는 토큰 시간 확인해야 함.
        //TODO: DB에는 4시간 더해서 들어가지만, Expired 계산할 때 문제 없는듯.
        //TODO: 고친것 같다.
        Token token = MongoDBHelper.docToToken(obj);
//        Logger.error(token.getEmail());
//        Logger.error(token.getUuid());
//        Logger.error(token.getCreationTime().toString());
//        Logger.error(token.getExpirationTime().toString());
//        Logger.error(String.valueOf(token.getIsSignUp()));

        return token;
    }

    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        Identity result = null;

        HashMap<String, Identity> tempUsers = new HashMap<>();
        DBCollection coll = MongoDBHelper.getDB().getCollection("user");
        DBCursor cursor = coll.find();

        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            Identity identity =  MongoDBHelper.docToIdentity(obj);
            tempUsers.put(identity.identityId().userId() + identity.identityId().providerId(), identity);
        }

        for( Identity user : tempUsers.values() ) {
            Option<String> optionalEmail = user.email();
            if ( user.identityId().providerId().equals(providerId) &&
                    optionalEmail.isDefined() &&
                    optionalEmail.get().equalsIgnoreCase(email))
            {
                result = user;
                break;
            }
        }

        return result;
    }

    @Override
    public void doDeleteToken(String uuid) {
        DBCollection coll = MongoDBHelper.getDB().getCollection("token");
        coll.remove(new BasicDBObject("uuid",uuid));
    }

    @Override
    public void doDeleteExpiredTokens() {
        HashMap<String, Token> tokens = new HashMap<>();
        DBCollection coll = MongoDBHelper.getDB().getCollection("token");
        DBCursor cursor = coll.find();

        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            Token token =  MongoDBHelper.docToToken(obj);
            tokens.put(token.uuid, token);
        }

        Iterator<Map.Entry<String,Token>> iterator = tokens.entrySet().iterator();
        while ( iterator.hasNext() ) {
            Map.Entry<String, Token> entry = iterator.next();
            if ( entry.getValue().isExpired() ) {
                coll.remove(new BasicDBObject("uuid",entry.getValue().getUuid()));
                iterator.remove();
            }
        }
    }
}
