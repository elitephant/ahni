/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package service;

import com.mongodb.*;
import play.Application;
import play.Logger;
import scala.Option;
import securesocial.core.*;
import securesocial.core.java.BaseUserService;

import securesocial.core.java.Token;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A Sample In Memory user service in Java
 *
 * Note: This is NOT suitable for a production environment and is provided only as a guide.
 * A real implementation would persist things in a database
 */
public class InMemoryUserService extends BaseUserService {
    private HashMap<String, Identity> users  = new HashMap<String, Identity>();
    private HashMap<String, Token> tokens = new HashMap<String, Token>();

    public InMemoryUserService(Application application) {
        super(application);
    }

    @Override
    public Identity doSave(Identity user) {
        DBCollection coll = MongoDBUserService.getDB().getCollection("users");
        coll.insert(MongoDBUserService.identityToDoc(user));

        users.put(user.identityId().userId() + user.identityId().providerId(), user);
        // this sample returns the same user object, but you could return an instance of your own class
        // here as long as it implements the Identity interface. This will allow you to use your own class in the
        // protected actions and event callbacks. The same goes for the doFind(UserId userId) method.
        return user;
    }

    @Override
    public void doSave(Token token) {
        DBCollection coll = MongoDBUserService.getDB().getCollection("tokens");
        coll.insert(MongoDBUserService.tokenToDoc(token));

        tokens.put(token.uuid, token);
    }

    @Override
    public Identity doFind(IdentityId userId) {
        DBCollection coll = MongoDBUserService.getDB().getCollection("users");
        DBObject obj = coll.findOne(new BasicDBObject("UserIdAndProviderId",userId.userId() + userId.providerId()));

        Identity identity = MongoDBUserService.docToIdentity(obj);

        return users.get(userId.userId() + userId.providerId());
    }

    @Override
    public Token doFindToken(String tokenId) {
        DBCollection coll = MongoDBUserService.getDB().getCollection("tokens");
        DBObject obj = coll.findOne(new BasicDBObject("uuid", tokenId));

        //TODO: DB에 들어가는 토큰 시간 확인해야 함.
        Token token = MongoDBUserService.docToToken(obj);
        Logger.error(token.getEmail());
        Logger.error(token.getUuid());
        Logger.error(token.getCreationTime().toString());
        Logger.error(token.getExpirationTime().toString());
        Logger.error(String.valueOf(token.getIsSignUp()));

        return tokens.get(tokenId);
    }

    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        HashMap<String, Identity> tempUsers = new HashMap<String, Identity>();
        DBCollection coll = MongoDBUserService.getDB().getCollection("users");
        DBCursor cursor = coll.find();

        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            Identity identity =  MongoDBUserService.docToIdentity(obj);
            tempUsers.put(identity.identityId().userId() + identity.identityId().providerId(), identity);
        }

        for( Identity user : tempUsers.values() ) {
            Option<String> optionalEmail = user.email();
            if ( user.identityId().providerId().equals(providerId) &&
                    optionalEmail.isDefined() &&
                    optionalEmail.get().equalsIgnoreCase(email))
            {
                Logger.error("User Email: "+user.email().get());
                break;
            }
        }

        Identity result = null;
        for( Identity user : users.values() ) {
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
        DBCollection coll = MongoDBUserService.getDB().getCollection("tokens");
        coll.remove(new BasicDBObject("uuid",uuid));

        Logger.error("지운다!");
        tokens.remove(uuid);
    }

    @Override
    public void doDeleteExpiredTokens() {
        HashMap<String, Token> tempTokens = new HashMap<String, Token>();
        DBCollection coll = MongoDBUserService.getDB().getCollection("tokens");
        DBCursor cursor = coll.find();

        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            Token token =  MongoDBUserService.docToToken(obj);
            tempTokens.put(token.uuid, token);
        }

        Iterator<Map.Entry<String,Token>> iterator = tokens.entrySet().iterator();
        while ( iterator.hasNext() ) {
            Map.Entry<String, Token> entry = iterator.next();
            if ( entry.getValue().isExpired() ) {
                Logger.error(entry.getValue().getEmail());
                Logger.error(entry.getValue().getCreationTime().toString());
                Logger.error(entry.getValue().getExpirationTime().toString());
                iterator.remove();
            }
        }
    }
}