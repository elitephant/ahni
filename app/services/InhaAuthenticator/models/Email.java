package services.InhaAuthenticator.models;

import play.data.validation.Constraints;

public class Email {
    @Constraints.Required
    public String email;
}
