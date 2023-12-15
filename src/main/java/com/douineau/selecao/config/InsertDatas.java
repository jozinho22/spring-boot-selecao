package com.douineau.selecao.config;

import com.douineau.selecao.model.Player;
import com.douineau.selecao.model.security.Role;
import com.douineau.selecao.model.security.AuthorizedUser;
import com.douineau.selecao.repository.PlayerRepository;
import com.douineau.selecao.repository.security.AuthorizedUserRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
public class InsertDatas {

    @Autowired
    private AuthorizedUserRepository uRepo;
    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("classpath:players.json")
    Resource sample;

    @EventListener(ApplicationReadyEvent.class)
    public void insert() throws IOException {

        System.out.println("Inserting the datas :");

        AuthorizedUser admin = new AuthorizedUser("joss@gmail.com", passwordEncoder.encode("joss"), List.of(Role.ADMIN));
        uRepo.save(admin);

        File sampleFile = sample.getFile();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        List<Player> products = mapper.reader().forType(new TypeReference<List<Player>>() {
        }).readValue(sampleFile);

        playerRepo.saveAll(products);

        System.out.println("------------------");
        System.out.println("Datas inserted ");
        System.out.println("------------------");
    }
}