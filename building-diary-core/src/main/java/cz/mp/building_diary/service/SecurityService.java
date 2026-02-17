package cz.mp.building_diary.service;

import cz.mp.building_diary.entity.User;

public interface SecurityService {

    User getCurrentUser();

    boolean isAdmin();
}