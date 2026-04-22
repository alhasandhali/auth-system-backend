package com.backend.app.auth.authappbackend.util;

import java.util.UUID;

public class UserIdParse {
    public static UUID parseUUID(String uuid) {
        return UUID.fromString(uuid);
    }
}
