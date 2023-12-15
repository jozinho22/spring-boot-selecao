package com.douineau.selecao.repository.security;

import com.douineau.selecao.model.security.AuthorizedUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AuthorizedUserRepository extends CrudRepository<AuthorizedUser, Long> {

    Optional<AuthorizedUser> findByEmail(String email);
}
