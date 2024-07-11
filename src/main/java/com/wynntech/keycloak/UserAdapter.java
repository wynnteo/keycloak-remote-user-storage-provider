package com.wynntech.keycloak;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class UserAdapter extends AbstractUserAdapter {
    private static final Logger LOGGER = Logger.getLogger(UserAdapter.class);
    private final User user;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, User user) {
        super(session, realm, storageProviderModel);
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public void setUsername(String username) {
        user.setUserName(username);
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public void setEmail(String email) {
        user.setEmail(email);
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        user.setFirstName(firstName);
    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        user.setLastName(lastName);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }

    public String getFirstAttribute(String name) {
        if (name.equals("phoneNumber")) {
            return user.getPhoneNumber();
        } else if (name.equals("gender")) {
            return user.getGender();
        } else {
            return super.getFirstAttribute(name);
        }
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (name.equals("phoneNumber")) {
            user.setPhoneNumber(value);
        } else if (name.equals("gender")) {
            user.setGender(value);
        } else {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (name.equals("phoneNumber")) {
            user.setPhoneNumber(values.get(0));
        } else if (name.equals("gender")) {
            user.setGender(values.get(0));
        } else {
            super.setAttribute(name, values);
        }
    }

    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
        all.putAll(attrs);
        all.add(UserModel.LAST_NAME, user.getLastName());
        all.add(UserModel.FIRST_NAME, user.getFirstName());
        all.add(UserModel.EMAIL, user.getEmail());
        all.add("phoneNumber", user.getPhoneNumber());
        all.add("gender", user.getGender());
        return all;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        if (name.equals("phoneNumber")) {
            List<String> phone = new LinkedList<>();
            phone.add(user.getPhoneNumber());
            return phone.stream();
        }else if (name.equals("gender")) {
            List<String> gender = new LinkedList<>();
            gender.add(user.getGender());
            return gender.stream();
        } else {
            return super.getAttributeStream(name);
        }
    }
}
