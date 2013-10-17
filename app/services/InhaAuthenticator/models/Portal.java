package services.InhaAuthenticator.models;

import play.data.validation.Constraints;

public class Portal {
    @Constraints.Required
    public String uid;
    @Constraints.Required
    public String pwd;
}
