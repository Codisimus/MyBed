package com.codisimus.plugins.mybed;

/**
 * An OwnedBed is a bed which only the owner can sleep in
 * Others can sleep in it if an Inn sign is present
 *
 * @author Codisimus
 */
public class User {
    public String name;
    public int healed = 0;

    /**
     * Constructs a new User
     * 
     * @param name The name of the Player
     */
    public User(String name) {
        this.name = name;
    }
}
