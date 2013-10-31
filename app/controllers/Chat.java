package controllers;

import models.chat.ChatRoom;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import services.InhaAuthenticator.SecureInha;
import views.html.chat.index;
import views.html.chat.chatRoom;

@SecureSocial.SecuredAction
@Security.Authenticated(SecureInha.class)
public class Chat extends Controller {
    /**
     * Display the home page.
     */
    public static Result index() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Logger.debug("@채팅방입장");
        return ok(index.render(user));
    }

    /**
     * Display the chat room.
     */
    public static Result chatRoom(String username, String key) {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        Logger.debug("@@채팅방입장");
        if(username == null || username.trim().equals("")) {
            flash("error", "올바른 닉네임을 입력해주세요");
            return redirect(routes.Chat.index());
        } else if (key == null || key.trim().equals("") || !ChatRoom.getChatRooms().containsKey(key)) {
            flash("error", "잘못된 접근입니다");
            return redirect(routes.Chat.index());
        }
        return ok(chatRoom.render(user, username, key));
    }

    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> chat(final String username, final String key) {
        Logger.debug("@@@채팅방입장");
        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){

                // Join the chat room.
                try {
                    ChatRoom.join(username, key, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
