package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.entity.User;

public interface SecurityService {

    User getCurrentUser();

    boolean isAdmin();

    boolean isEmailVerified();
}