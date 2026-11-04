package com.github.regyl.loadbalancer.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class WeightedInstanceModel extends InstanceModel {

    private final int weight;

    public WeightedInstanceModel(String address, int weight) {
        super(address);
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        WeightedInstanceModel that = (WeightedInstanceModel) o;
        return weight == that.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), weight);
    }

    @Override
    public String toString() {
        return "WeightedInstanceModel{"
                + "weight=" + weight
                + ", address='" + address + '\''
                + "}";
    }
}
