<template>
  <div class="main-container">
    <div class="flight-header">
      <h1 >Available Flights</h1>
    </div>
    <div class="flight-list-page">
      <div class="filters">
        <div class="filter-item">
          <h3>Number of passangers</h3>
          <select v-model="userParams.nrOfPassangers">
            <option v-for="people in numbersList" :value="people" :key="people">{{ people }}</option>
          </select>
        </div>
        <div class="filter-item">
          <h3>Seat preferences</h3>
          <select v-model="userParams.seatPreference" class="multiselect" id="seat-preferences" multiple>
            <option v-for="preference in preferenceSelection" :value="preference" :key="preference">{{ preference }}</option>
          </select>
        </div>

        <input v-model="filters.destination" placeholder="Destination" />
        <input type="date" v-model="filters.date" />
        <input type="number" v-model="filters.price" placeholder="Max Price" />
        <button id="filter-button" @click="applyFilters">Apply Filters</button>
      </div>

      <ul>
        <li class="flight-listing" v-for="flight in filteredFlights" :key="flight.id" @click="selectFlight(flight.id)">
          <div class="flight-listing-left">
            <h2>{{formatTime(flight.departureDate, "date")}}</h2>
          </div>
          <div class="flight-listing-middle">
            <div class="time-location-box">
              <p id="time" > {{ formatTime(flight.departureDate, "time")}} </p>
              <p> {{flight.source}} </p>
            </div>
            <div class="flight-duration-box">
              <p>{{ flight.company }} </p>
              <div class="flight-line">
                <img src="@/assets/flightplanner/take-off.svg" class="airplane-icon" />
                <hr class="flight-path" />
              </div>
              <p>{{calculateDuration(flight)}}</p>
            </div>
            <div class="time-location-box">
              <p id="time" > {{ formatTime(flight.arrivalDate, "time")}} </p>
              <p> {{flight.destination}} </p>
            </div>
          </div>
          <div class="flight-listing-right">
            <h2> {{ flight.price }} €</h2>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>

import axios from 'axios'
import {computed, onMounted, reactive, ref, watch, watchEffect} from "vue";
import { useRouter} from 'vue-router';
import {useBookingStore} from "@/stores/booking.js";

const router = useRouter();
const bookingStore = useBookingStore();

// numbers for selecting amout of people travelling
const numbersList = Array.from({ length: 50 }, (_, i) => i + 1);
const flights = ref()
const filters = reactive({
  destination: '',
  date: '',
  price: ''
})

const preferenceSelection = ["window", "legroom", "exit"]

const userParams = reactive({
  nrOfPassangers: null,
  seatPreference: [],
})

const filteredFlights = ref([])

//For formatting time for the front end list elements
function formatTime(timestamp, format = "time") {
  const date = new Date(timestamp);
  if (format === "time") {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }
  if (format === "date") {
    const month = date.toLocaleDateString("en-US", {month: "short"});
    const day = String(date.getDate()).padStart(1, '0');
    return `${month} ${day}`;
  }
}

//For calculating the duration of flight
function calculateDuration(flight) {
  const departure = new Date(flight.departureDate);
  const arrival = new Date(flight.arrivalDate);

  const diff = arrival.getTime() - departure.getTime();
  const totalMinutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  return `${hours}h ${minutes}m`;
}


//fetching the flights data from the backend
function fetchFlights() {
  axios.get('http://localhost:8080/api/flights')
      .then(response => {
        flights.value = response.data
        filteredFlights.value = flights.value
        console.log(filteredFlights.value)
      })
      .catch(error => console.error('Error fetching flights:', error))
}


//Appling filters on the flights
function applyFilters() {
  filteredFlights.value = flights.value.filter(flight => {
    const matchesDestination = filters.destination
        ? flight.destination.toLowerCase().includes(filters.destination.toLowerCase())
        : true
    const matchesDate = filters.date
        ? flight.departureDate.startsWith(filters.date)
        : true
    const matchesPrice = filters.price
        ? flight.price <= filters.price
        : true
    return matchesDestination && matchesDate && matchesPrice
  })
}

function selectFlight(flightId) {
  if(userParams.nrOfPassangers < 1) {
    alert("Please select at least one passenger to continue.");
    return;
  }
  bookingStore.setBooking(parseInt(userParams.nrOfPassangers), userParams.seatPreference)
  router.push(`/seats`)
}

onMounted(() => {
  //fetching flight data on mount
  fetchFlights()

  //Imported multiselect tag for selecting multiple preferences for a seat selection
  if (typeof MultiSelectTag !== 'undefined') {
    new MultiSelectTag('seat-preferences', {
      rounded: true,    // default true
      shadow: true,      // default false
      placeholder: 'Search',  // default Search...
      tagColor: {
        textColor: '#327b2c',
        borderColor: '#92e681',
        bgColor: '#eaffe6',
      },
      onChange: function(values) {
        userParams.seatPreference = values;
      }
    })
  }
})

</script>

<style scoped>
.main-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100vw;
  height: 100vh;
  background-color: #f8f8f8;
  --primary-color: #2c3e50;
  --secondary-color: #2ecc71;
  --text-color: #333333;
  --background-color: #f4f4f4;
  --listing-color: #e8e8e8;
  --button-color: #a1da71;
  --button-color-dark: #6dcfaf;

}

.flight-header {
  height: 3em;
  font-size: 2em;
  background-color: var(--primary-color);
  color: white;
  width: 100%;
  display: flex;
  justify-content: center;

}


.flight-list-page {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: stretch;
  padding: 20px;
  gap:20px;

}

.filters {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  margin-bottom: 20px;
  gap: 20px;
}

.filters input, .filters select{
  height: 50px;
  border-radius: 10px;
  border: solid 1px;
  padding: 10px;
  font-size: 20px;
  margin-right: 10px;
}
.filters input {
  width: 80%;
}
.filters select {
  width: 100%;
}

.filter-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 80%;
}

select{
  text-align: center;
}

ul {
  flex: 3;
  list-style-type: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: start;
  gap:20px;
}

li{
  background-color: var(--listing-color);
  border-bottom-left-radius: 15px;
  border-top-left-radius: 15px;
  width: 80%;
}

#filter-button {
  height: 50px;
  width: 40%;
  border: none;
  border-radius: 15px;
  background-color: var(--button-color);
}

.flight-listing{
  width: 100%;
  height: auto;
  display: flex;
  flex-direction: row;
  box-shadow: rgba(99, 99, 99, 0.2) 0px 2px 8px 0px;
}
.flight-listing-right{
  display: flex;
  min-width: 150px;
  background-color: #d3d3d3;
  justify-content: center;
  align-items: center;
}

.flight-listing-middle{
  width: 100%;
  display: flex;
  flex-direction: row;
}
.flight-listing-left{
  min-width: 150px;
  display: flex;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: center;
  background-color: #d3d3d3;
  border-bottom-left-radius: 15px;
  border-top-left-radius: 15px;
}
.flight-listing-left h2, .flight-listing-right h2{
  width: 50%;
  font-size: 1.5em;
}



.time-location-box{
  min-width: 80px;
  width: 20%;
  min-height: 80px;
  margin: 20px 30px 20px 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 15px;

}

#time{
  font-size: 2em;
}



.flight-duration-box {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
}

.flight-line {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  padding: 20px;
}

.flight-path {
  flex: 1;
  border: none;
  border-radius: 1px;
  border-top: 2px solid #ccc;
  margin: 0;
}

.airplane-icon {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 2; /* Make sure the icon appears above the line */
  background-color: var(--listing-color);
  width: 50px;
  padding: 10px;
}

.duration {
  font-size: 0.8rem;
  color: #666;
  margin: 5px 0 0 0;
}


</style>
