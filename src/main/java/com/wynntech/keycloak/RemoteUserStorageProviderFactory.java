package com.wynntech.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.jboss.logging.Logger;
import org.keycloak.storage.UserStorageProviderFactory;

public class RemoteUserStorageProviderFactory implements UserStorageProviderFactory<RemoteUserStorageProvider>  {
    private static final Logger logger = Logger.getLogger(RemoteUserStorageProviderFactory.class);
    public static final String PROVIDER_NAME = "remote-mysql-user-storage-spi";

    @Override
    public RemoteUserStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        return new RemoteUserStorageProvider(keycloakSession, componentModel, new UserService(keycloakSession));
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }
}
