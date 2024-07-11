package com.wynntech.keycloak;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

public class RemoteUserStorageProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider, CredentialInputValidator {
    private static final Logger LOGGER = Logger.getLogger(RemoteUserStorageProvider.class);
    private KeycloakSession session;
    private ComponentModel model;

    private UserService userService;


    public RemoteUserStorageProvider(KeycloakSession session, ComponentModel model, UserService userService) {
        this.session = session;
        this.model = model;
        this.userService = userService;
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String s) {
        StorageId storageId = new StorageId(s);
        String email = storageId.getExternalId();
        return getUserByUsername(realmModel, email);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realmModel, String s) {
        UserModel returnValue = null;
        User user = userService.getUserByUserName(s);
        if(user!=null) {
            returnValue = new UserAdapter(session, realmModel, model, user);
        }
        return returnValue;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realmModel, String s) {
        return getUserByUsername(realmModel, s);
    }

    @Override
    public boolean supportsCredentialType(String s) {
        return PasswordCredentialModel.TYPE.equals(s);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String s) {
        return s.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        return userService.verifyUserPassword(userModel.getUsername(),
            credentialInput.getChallengeResponse());
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params, Integer firstResult, Integer maxResults) {
        String search = params.get(UserModel.SEARCH);
        LOGGER.error(search);
        if (search == null || search.isEmpty()) {
            List<UserModel> keycloakUsers = session.users().searchForUserStream(realmModel, "")
                .skip(firstResult != null ? firstResult : 0)
                .limit(maxResults != null ? maxResults : Integer.MAX_VALUE)
                .collect(Collectors.toList());
            return keycloakUsers.stream();
        }
        // Call the UserService to get the list of users
        List<User> users = userService.searchUsers(search, firstResult, maxResults);
        // Convert the list of User objects to a stream of UserModel objects
        return users.stream().map(user -> new UserAdapter(session, realmModel, model, user));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return Stream.empty();
    }
}
