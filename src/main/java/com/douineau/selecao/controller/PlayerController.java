package com.douineau.selecao.controller;

import com.douineau.selecao.model.Player;
import com.douineau.selecao.repository.PlayerRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {

    private final PlayerRepository playerRepo;

    @Autowired
    public PlayerController(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @Value("classpath:products.json")
    Resource sample;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<Player> findAll() {
        return playerRepo.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Player findById(@PathVariable Long id) {
        return playerRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Content not existing in db"
                        )
                );
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> create(@Valid @RequestBody Player p, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(errors.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        playerRepo.save(p);

        return ResponseEntity.ok("Le produit n°" + p.getId() + " a bien été créé");
    }


    // insert datas
    /*@PostMapping("/sample")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createDatas() throws IOException {

        File sampleFile = sample.getFile();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        List<Product> products = mapper.reader().forType(new TypeReference<List<Product>>() {
        }).readValue(sampleFile);

        prodRepo.saveAll(products);

        return ResponseEntity.ok("Les " + products.size() + " produits ont bien été créés");
    }*/

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Player> put(@PathVariable Long id, @Valid @RequestBody Player pDetails, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(errors.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        Player p = playerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for the id : " + id));

        playerRepo.save(pDetails);
        return ResponseEntity.ok(pDetails);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> delete(@PathVariable Long id) {

        Player p = playerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for the id : " + id));

        playerRepo.delete(p);
        return ResponseEntity.ok("Le produit n°" + id + " a bien été supprimé" );
    }


}
