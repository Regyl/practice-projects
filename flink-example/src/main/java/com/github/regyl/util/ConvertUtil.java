package com.github.regyl.util;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.experimental.UtilityClass;

@UtilityClass 
public class ConvertUtil {

    public String getHostname(JsonNode node) {
        if (node.has("source_hostname")) {
            return node.get("source_hostname").asText();
        } else if (node.has("_HOSTNAME")) {
            return node.get("_HOSTNAME").asText();
        }

        return "undefined";
    }
}
