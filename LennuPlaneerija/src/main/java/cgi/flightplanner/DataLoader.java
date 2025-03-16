package cgi.flightplanner;

import cgi.flightplanner.model.Flight;
import cgi.flightplanner.repository.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(FlightRepository flightRepository) {
        return args -> {
            // Optional: Clear any existing flights
            flightRepository.deleteAll();

            // Create sample flights
            Flight flight1 = new Flight(
                    "Tallinn",
                    "Zurich",
                    LocalDateTime.of(2025, 7, 1, 10, 15),
                    LocalDateTime.of(2025, 7, 1, 12, 25),
                    150.0,
                    "Ryanair"
            );

            Flight flight2 = new Flight(
                    "Tallinn",
                    "London",
                    LocalDateTime.of(2025, 4, 2, 9, 30),
                    LocalDateTime.of(2025, 4, 2, 11, 30),
                    120.0,
                    "AirBaltic"
            );

            Flight flight3 = new Flight(
                    "Tallinn",
                    "Paris",
                    LocalDateTime.of(2025, 12, 12, 14, 0),
                    LocalDateTime.of(2025, 12, 12, 17, 15),
                    200.0,
                    "Lufthansa"
            );

            Flight flight4 = new Flight(
                    "Tallinn",
                    "Rome",
                    LocalDateTime.of(2025, 4, 4, 16, 25),
                    LocalDateTime.of(2025, 4, 4, 19, 55),
                    180.0,
                    "EasyJet"
            );

            // Save sample flights to the database
            flightRepository.save(flight1);
            flightRepository.save(flight2);
            flightRepository.save(flight3);
            flightRepository.save(flight4);

            System.out.println("Sample flights loaded into the database.");
        };
    }
}
