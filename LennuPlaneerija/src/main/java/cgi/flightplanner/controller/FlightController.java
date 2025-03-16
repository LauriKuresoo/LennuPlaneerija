package cgi.flightplanner.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import cgi.flightplanner.model.Flight;
import cgi.flightplanner.repository.FlightRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://vue-frontend:5173"})
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightRepository flightRepository;

    public FlightController(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    // GET endpoint to retrieve all flights
    @GetMapping
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
}
