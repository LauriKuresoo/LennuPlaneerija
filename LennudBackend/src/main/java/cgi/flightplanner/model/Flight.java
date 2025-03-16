package cgi.flightplanner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(name = "departure_date", nullable = false)
    private LocalDateTime departureDate;

    @Column(name = "arrival_date", nullable = false)
    private LocalDateTime arrivalDate;
    
    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String company;

    public Flight(String source, String destination, LocalDateTime departureDate, LocalDateTime arrivalDate, double price, String company) {
        this.source = source;
        this.destination = destination;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.price = price;
        this.company = company;
    }
}
