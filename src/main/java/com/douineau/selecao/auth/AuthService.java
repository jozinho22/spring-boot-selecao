package com.douineau.selecao.auth;

import com.douineau.selecao.model.security.Role;
import com.douineau.selecao.model.security.AuthorizedUser;
import com.douineau.selecao.repository.security.AuthorizedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.douineau.selecao.security.JWTService;

import java.util.*;

@Service
public class AuthService {

    private final AuthorizedUserRepository uRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService JWTService;
    private AuthenticationManager authManager;

    @Autowired
    public AuthService(AuthorizedUserRepository uRepo,
                       PasswordEncoder passwordEncoder,
                       JWTService JWTService,
                       AuthenticationManager authManager) {
        this.uRepo = uRepo;
        this.passwordEncoder = passwordEncoder;
        this.JWTService = JWTService;
        this.authManager = authManager;
    }

    public AuthResponse register(AuthRequest req) {

        if(uRepo.findByEmail(req.getEmail()).isPresent()) {
            return new AuthResponse(null, HttpStatus.valueOf(409), "This mail is already taken");
        }

        var user = new AuthorizedUser(
                req.getEmail(),
                passwordEncoder.encode(req.getPassword()),
                List.of(Role.USER));

        uRepo.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities());

        var jwtToken = JWTService.generateToken(claims, user);
        System.out.println(jwtToken);
        
        return new AuthResponse(jwtToken, HttpStatus.CREATED, "Your token is valid for 24h");
    }

    public AuthResponse authenticate(AuthRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );
        var user = uRepo.findByEmail(req.getEmail());
        if(user.isEmpty()) {
            return new AuthResponse(null, HttpStatus.valueOf(404), "This user does not exist");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.get().getAuthorities());

        var jwtToken = JWTService.generateToken(claims, user.get());
        System.out.println(jwtToken);

        return new AuthResponse(jwtToken, HttpStatus.OK, "Your token is valid for 24h");
    }


}
