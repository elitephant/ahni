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
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("ds041168.mongolab.com",41168);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        DB db = mongoClient.getDB("ahni");
        db.authenticate("admin","1234".toCharArray());
        DBCollection coll = db.getCollection("users");

        BasicDBObject doc = new BasicDBObject("UserIdAndProviderId", user.identityId().userId() + user.identityId().providerId())
                .append("authMethod", new BasicDBObject("method",user.authMethod().method()))
                .append("firstName", user.firstName())
                .append("fullName", user.fullName())
                .append("identityId", new BasicDBObject("productPrefix",user.identityId().productPrefix())
                        .append("providerId",user.identityId().providerId())
                        .append("userId",user.identityId().userId()))
                .append("lastName", user.lastName());

        if(user.email().nonEmpty()){
            doc.append("email", user.email().get());
        }
        if(user.avatarUrl().nonEmpty()){
            doc.append("avatarUrl", user.avatarUrl().get());
        }

        if(user.oAuth1Info().nonEmpty()) {
            doc.append("oAuth1Info", new BasicDBObject("secret", user.oAuth1Info().get().secret())
                    .append("token",user.oAuth1Info().get().token()));
        }
        if(user.oAuth2Info().nonEmpty()) {
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

        if(user.passwordInfo().nonEmpty()) {
            BasicDBObject passwordInfo = new BasicDBObject("password", user.passwordInfo().get().password())
                    .append("hasher",user.passwordInfo().get().hasher());
            if(user.passwordInfo().get().salt().nonEmpty()){
                passwordInfo.append("salt",user.passwordInfo().get().salt().get());
            }
            doc.append("passwordInfo", passwordInfo);
        }
        coll.insert(doc);

        users.put(user.identityId().userId() + user.identityId().providerId(), user);
        // this sample returns the same user object, but you could return an instance of your own class
        // here as long as it implements the Identity interface. This will allow you to use your own class in the
        // protected actions and event callbacks. The same goes for the doFind(UserId userId) method.
        return user;
    }

    @Override
    public void doSave(Token token) {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("ds041168.mongolab.com",41168);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        DB db = mongoClient.getDB("ahni");
        db.authenticate("admin","1234".toCharArray());
        DBCollection coll = db.getCollection("tokens");

        BasicDBObject doc = new BasicDBObject("uuid", token.getUuid())
                .append("token", new BasicDBObject("email",token.getEmail())
                        .append("creationTime",token.getCreationTime().toDate())
                        .append("expirationTime",token.getExpirationTime().toDate())
                        .append("isSignUp",token.getIsSignUp()));
        coll.insert(doc);

        tokens.put(token.uuid, token);
    }

    @Override
    public Identity doFind(IdentityId userId) {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("ds041168.mongolab.com",41168);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        DB db = mongoClient.getDB("ahni");
        db.authenticate("admin","1234".toCharArray());
        DBCollection coll = db.getCollection("users");
        DBObject obj = coll.findOne(new BasicDBObject("UserIdAndProviderId",userId.userId() + userId.providerId()));

        Identity ret = MongoDBUserService.docToIdentity(obj);

        return users.get(userId.userId() + userId.providerId());
    }

    @Override
    public Token doFindToken(String tokenId) {
        return tokens.get(tokenId);
    }

    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
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
        tokens.remove(uuid);
    }

    @Override
    public void doDeleteExpiredTokens() {
        Iterator<Map.Entry<String,Token>> iterator = tokens.entrySet().iterator();
        while ( iterator.hasNext() ) {
            Map.Entry<String, Token> entry = iterator.next();
            if ( entry.getValue().isExpired() ) {
                iterator.remove();
            }
        }
    }
}