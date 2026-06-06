package com.example.flightbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "flights")
@Component
public class FlightCatalogProperties {

    private List<Entry> catalog = new ArrayList<>();

    public List<Entry> getCatalog() {
        return List.copyOf(catalog);
    }

    public void setCatalog(List<Entry> catalog) {
        this.catalog = new ArrayList<>(catalog);
    }

    public static class Entry {
        private String number;
        private int seats;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public int getSeats() {
            return seats;
        }

        public void setSeats(int seats) {
            this.seats = seats;
        }
    }
}