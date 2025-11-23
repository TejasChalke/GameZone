package com.sl.gamezone.dto;

import com.sl.gamezone.model.user.GenericUser;

import java.util.List;

public class GenericResponse {
    public boolean lobbyClosed;
    public List<GenericUser> users;

    public GenericResponse() {
        this.lobbyClosed = false;
        this.users = null;
    }

    public GenericResponse(boolean lobbyClosed, List<GenericUser> users) {
        this.lobbyClosed = lobbyClosed;
        this.users = users;
    }
}
