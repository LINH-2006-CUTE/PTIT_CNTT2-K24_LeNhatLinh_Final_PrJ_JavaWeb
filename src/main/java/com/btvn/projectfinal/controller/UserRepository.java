package com.btvn.projectfinal.controller;

import com.btvn.projectfinal.model.entity.User;
import org.springframework.data.repository.Repository;

interface UserRepository extends Repository<User, Long> {
    boolean existsByUsername(String name);
}
