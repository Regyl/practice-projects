package com.github.regyl.hotelbooking.model;

import java.util.concurrent.locks.ReentrantLock;

public class RoomModel {

    private final Long id;
    private final ReentrantLock lock;

    public RoomModel(Long id) {
        this.id = id;
        this.lock = new ReentrantLock(); //fair is important
    }

    public Long getId() {
        return id;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
