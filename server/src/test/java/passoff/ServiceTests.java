package passoff;

import dataaccess.implementations.*;
import exception.DataAccessException;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTests {
    static final MemoryUserDAO userDao = new MemoryUserDAO();
    static final MemoryGameDAO gameDao = new MemoryGameDAO();
    static final MemoryAuthDAO authDao = new MemoryAuthDAO();
    static final GameService gameService = new GameService(gameDao, authDao);
    static final UserService userService = new UserService(userDao, authDao);
    

    @BeforeEach
    void clear() throws DataAccessException {
        gameService.clear();
        userService.clear();
    }

    @Test
    void successRegisterUser() throws DataAccessException {
        var user = new UserData("fourarms", "Shoji#16", "fourarms216@gmail.com");
        userService.registerUser(user);
        var userList = userService.getAllUsers();
        assertEquals(1, userList.size());
        assertTrue(userList.containsKey(user.username()));
    }

    @Test
    void doubleRegisterUser() throws DataAccessException {
        var user = new UserData("fourarms", "Shoji#16", "fourarms216@gmail.com");
        userService.registerUser(user);
        DataAccessException ex = assertThrows(DataAccessException.class,() -> userService.registerUser(user));
        assertEquals(403, ex.StatusCode());
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void listGames() throws DataAccessException {
        List<Integer> expected = new ArrayList<>();
        expected.add(gameService.createGame("game1", "123"));
        expected.add(gameService.createGame("game2", "123"));
        expected.add(gameService.createGame("game3", "123"));

        var actual = gameService.onlyGames();
        assertIterableEquals(expected, actual);
    }

    @Test
    void deletePet() throws DataAccessException {
        List<Pet> expected = new ArrayList<>();
        var pet = service.addPet(new Pet(0, "joe", PetType.FISH));
        expected.add(service.addPet(new Pet(0, "sally", PetType.CAT)));
        expected.add(service.addPet(new Pet(0, "fido", PetType.DOG)));

        service.deletePet(pet.id());
        var actual = service.listPets();
        assertIterableEquals(expected, actual);
    }

    @Test
    void deleteAllPets() throws DataAccessException {
        service.addPet(new Pet(0, "joe", PetType.FISH));
        service.addPet(new Pet(0, "sally", PetType.CAT));
        service.addPet(new Pet(0, "fido", PetType.DOG));

        service.deleteAllPets();
        assertEquals(0, service.listPets().size());
    }

    @Test
    void noDogsWithFleas() {
        assertThrows(DataAccessException.class, () ->
                service.addPet(new Pet(0, "fleas", PetType.DOG)));
    }
}