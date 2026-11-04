package com.github.regyl.loadbalancer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class InstanceModel {

    protected final String address; //maybe InetAddress

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstanceModel instance = (InstanceModel) o;
        return Objects.equals(address, instance.address);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(address);
    }

    @Override
    public String toString() {
        return "InstanceModel{"
                + "address='" + address + '\''
                + '}';
    }
}
