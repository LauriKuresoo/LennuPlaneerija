<script setup>
import Seat from "@/components/seating/Seat.vue";
import SeatReferenceMap from "@/components/seating/SeatReferenceMap.vue";
import seatLayoutData from '@/data/seat_configuration.json';
import wingImage from '@/assets//flightplanner/seat-map-wing-desktop.svg';
import {computed, nextTick, onBeforeMount, onMounted, onUnmounted, onUpdated, reactive, ref} from "vue";
import {useBookingStore} from "@/stores/booking.js";


const bookingStore = useBookingStore();
let booking = reactive({})

const seatMap = ref()
const adjacentSeatsMap = ref(new Map())

const selectedSeats = ref([])
const suggestedSeats = ref([])

var smallestWingRow = null, biggestWingRow = null;
const wingRefs = reactive({
  smallestWingRowRef: [], biggestWingRowRef: [],
});
const wingMultiplier = 1.74 //to make the wing image proportional to the seats

function deselectSeat(seatNr = null){
  let index = 0
  if(seatNr !== null) index = selectedSeats.value.indexOf(seatNr)
  selectedSeats.value.splice(index, 1)
}

function selectSeat(seat) {
  if (seat.occupied) return; // if occupied then don't select

  if (selectedSeats.value.includes(seat.seatNumber)) deselectSeat(seat.seatNumber); //if the seat is selected then deselect it
  else if (!selectedSeats.value.includes(seat.seatNumber)) {
    if (selectedSeats.value.length === booking.nrOfPeople) deselectSeat() //if too many selected seats then deselect one seat
    selectedSeats.value.push(seat.seatNumber);
    return;
  }
}

//Suggests next seats to the customer
function suggestNextSeats(){
  suggestedSeats.value = [];
  suggestSeats(booking.nrOfPeople)
}

function selectSuggestedSeats(){
  selectedSeats.value = suggestedSeats.value;
}

//suggests seats to given nr of people
function suggestSeats(nrOfPeople) {
    let keys = Object.keys(adjacentSeatsMap.value).map(Number);
    let maxKey = Math.max(...keys);
    let groups = [nrOfPeople];

    //if there are more people than a row can contain then suggest seats for subgroups
    if (maxKey < booking.nrOfPeople) {
      const nrOfGroups = Math.ceil(nrOfPeople / maxKey);
      groups = divideGroup(nrOfPeople, nrOfGroups);
    }
    for (const group of groups) {
      let keys = Object.keys(adjacentSeatsMap.value).map(Number);
      let maxKey = Math.max(...keys);
      suggestSeatsForGroup(group, maxKey);
    }
}

function suggestSeatsForGroup(nrOfPeople, maxKey){
    for (let i = nrOfPeople; i <= maxKey; i++) {
      if (adjacentSeatsMap.value[i]) {
        adjacentSeatsMap.value[i].sort((a, b) => b.preferencesMet - a.preferencesMet);
        // We take the row where there are most preferences met for the amount of people.
        const result = adjacentSeatsMap.value[i][0];
        // Remove the row to not suggest it again
        adjacentSeatsMap.value[i].splice(0, 1);
        if (adjacentSeatsMap.value[i].length === 0) {
          delete adjacentSeatsMap.value[i];
        }

        const row = result.rowIndex;
        const startIndex = result.start;
        let endIndex = result.end;

        const seats = seatMap.value.seatLayout[row].layout;

        // Collect seat numbers
        if (endIndex - startIndex > booking.nrOfPeople) endIndex = startIndex + booking.nrOfPeople;
        for (let j = startIndex; j <= endIndex; j++) {
          const seat = seats[j];
          if (typeof seat === "object") {
            suggestedSeats.value.push(seat.seatNumber);
          }
        }
        return suggestedSeats.value;
      }
    }
}



function divideGroup(groupSize, divisions) {
  // Base distribution - minimum people per division
  const baseDistribution = Math.floor(groupSize / divisions);
  // How many divisions need an extra person
  const remainder = groupSize % divisions;
  // Create the distribution array
  const result = Array(divisions).fill(baseDistribution);

  // Add the remainder starting from the first division
  for (let i = 0; i < remainder; i++) {
    result[i]++;
  }
  return result;
}
// Finds the div elements where class == "wing" and where it is the first row of wing seats or the last.
function setWingRefs(el, seat, row){
  const seatClass = seat.toLowerCase();
  if (seatClass === "wing") {
    if(row === smallestWingRow.row && !wingRefs.smallestWingRowRef.includes(el))
      wingRefs.smallestWingRowRef.push(el);
    else if(row === biggestWingRow.row && !wingRefs.biggestWingRowRef.includes(el))
      wingRefs.biggestWingRowRef.push(el);
  }
  // If it is a random class then add the class name to the div element
  el.classList.add(seatClass);
}

// Adds the wings to the plane
function addWingImages() {
  let turnAround = true; // For the other wing to mirror on y-axis

  // Getting the div locations for wing start position and width.
  const biggestRowBox = wingRefs.biggestWingRowRef[0].getBoundingClientRect()
  const smallestRowBox1 = wingRefs.smallestWingRowRef[0].getBoundingClientRect()
  const smallestRowBox2 = wingRefs.smallestWingRowRef[0].getBoundingClientRect()
  const x_min = Math.min(smallestRowBox1.x, smallestRowBox2.x);
  const y_max = biggestRowBox.y
  const y_min = smallestRowBox1.y
  const height = (y_max - y_min)*wingMultiplier;

  for(const wingRef of wingRefs.smallestWingRowRef){
    turnAround = !turnAround;
    const img = document.createElement("img");
    img.src = wingImage;
    img.style.position = "absolute";

    if (!turnAround) img.style.transform = "scaleX(-1)"; // Flips the image vertically

    img.onload = function () {
      let aspectRatio = img.naturalWidth / img.naturalHeight;
      if(wingRef.getBoundingClientRect().x === x_min) {
        img.style.left = `${-(height * aspectRatio)}px`;
      }
      img.style.height = `${height}px`;
      img.style.width = `${height * aspectRatio}px`;
    };
    wingRef.style.position = "relative";
    wingRef.appendChild(img);

    console.log("image loaded", img);
  }
}

function generateRandomSeating(seatData, occupancyRate = 0.5) {
  const newSeatData = JSON.parse(JSON.stringify(seatLayoutData));

  newSeatData.seatLayout.forEach(row => {
    row.layout.forEach(item => {
      // Check if the item is a seat object (not "WINDOW" or "CORRIDOR" strings)
      if (typeof item === 'object' && item !== null && 'seatNumber' in item) {
        // Randomly set the occupied status based on the occupancy rate
        item.occupied = Math.random() < occupancyRate;
      }
    });
  });

  return newSeatData;
}
function processAdjacentSeats() {
  seatMap.value.seatLayout.forEach((rowData, rowIndex) => {
    const layout = rowData.layout;
    let currentAdjacentCount = 0;
    let currentPreferencesMet = 0;
    let maxAdjacentCount = 0;
    let maxPreferencesMet = 0;
    let startPosition = -1;
    let endPosition = -1;
    let maxStartPosition = -1;
    let maxEndPosition = -1;

    // Iterate through the layout to find adjacent unoccupied seats
    for (let i = 0; i < layout.length; i++) {
      const item = layout[i];

      // Check if the item is a seat object and not occupied
      if (typeof item === 'object' && !item.occupied) {
        // Start tracking a new sequence if not already tracking
        if (currentAdjacentCount === 0) {
          startPosition = i;
        }
        currentAdjacentCount++;
        currentPreferencesMet += booking.preferences.reduce((sum, preference) => {
          return item[preference.value] ? sum + 1 : sum;
        }, 0);

      } else {
        if(item === "CORRIDOR") continue;
        // End of a sequence of adjacent seats
        if (currentAdjacentCount > 0) {
          endPosition = i - 1;
          // Update max if current sequence is longer
          if (currentAdjacentCount > maxAdjacentCount) {
            maxPreferencesMet = currentPreferencesMet
            maxAdjacentCount = currentAdjacentCount;
            maxStartPosition = startPosition;
            maxEndPosition = endPosition;
          }
          // Reset counter for next sequence
          currentAdjacentCount = 0;
          currentPreferencesMet = 0;
        }
      }
    }

    // Check if we ended the row with a sequence
    if (currentAdjacentCount > 0) {
      endPosition = layout.length - 1;
      if (currentAdjacentCount > maxAdjacentCount) {
        maxPreferencesMet = currentPreferencesMet;
        maxAdjacentCount = currentAdjacentCount;
        maxStartPosition = startPosition;
        maxEndPosition = endPosition;
      }
    }

    // Only process rows that have at least one adjacent seat
    if (maxAdjacentCount > 0) {
      // Create or update entry in the map
      if (!adjacentSeatsMap.value[maxAdjacentCount]) {
        adjacentSeatsMap.value[maxAdjacentCount] = [];
      }

      adjacentSeatsMap.value[maxAdjacentCount].push({
        rowIndex,
        preferencesMet: maxPreferencesMet,
        row: rowData.row,
        start: maxStartPosition,
        end: maxEndPosition
      });
    }
  });
  console.log("adjacentSeatsMap.value", adjacentSeatsMap.value);
}


onBeforeMount(() => {
  booking = {
    preferences: bookingStore.preferences,
    nrOfPeople: bookingStore.nrOfPeople,
  }
  seatMap.value = generateRandomSeating(seatLayoutData)
  nextTick()
  var previousRow = seatMap.value.seatLayout[0]
  for (var row of seatMap.value.seatLayout){
    if(row.layout.includes("WING") && smallestWingRow === null) smallestWingRow = row;
    else if (!row.layout.includes("WING") && previousRow.layout[0] === "WING"){
      biggestWingRow = previousRow;
    }
    previousRow = row;
  }
  processAdjacentSeats()
})

onMounted(async () => {
  console.log(bookingStore.preferences)
  console.log("suggested seats: ", suggestSeats(booking.nrOfPeople));
  await nextTick(); // Ensure DOM is fully rendered
  addWingImages()
})

</script>

<template>
  <div class="container">
    <div class="seat-selector">
      <div v-for="row in seatMap.seatLayout" :key="row.row" class="row">
        <div v-for="(seat, index) in row.layout" :key="typeof seat === 'string' ? seat + index + row.row : seat.seatNumber">
          <Seat v-if="typeof seat !== 'string'"
                :seat="seat"
                :style-props="{selected: selectedSeats.includes(seat.seatNumber),
                            suggested: suggestedSeats.includes(seat.seatNumber)}"
                @click="selectSeat(seat)">
            {{ seat.seatNumber }}
          </Seat>
          <div v-else :ref="el => setWingRefs(el, seat, row.row)"></div>
        </div>
      </div>
    </div>

    <div class="selection-tab">
      <SeatReferenceMap/>
      <button id="suggest-btn" @click="suggestNextSeats()"> Next Suggested Seats </button>
      <button id="suggest-btn" @click="selectSuggestedSeats()">Select Suggested Seats</button>
    </div>
  </div>
</template>

<style>

.container {
  display: flex;
  flex-direction: row;
  width: 100vw;
  height: 100vh;
}

.seat-selector {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  flex: 3;
  order: 2;
}

.selection-tab {
  margin: 100px 50px;
  padding: 50px;
  border-radius: 30px;
  flex: 1;
  order: 1;
  background-color: #e0e0e7;
  width: 100%;
  height: 70%;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap:20px;
  align-items: center;
}

#suggest-btn {
  height: 50px;
  width: 80%;
  border-radius: 20px;
  padding: 5px;
  border: none;
  background-color: #242323;
  color: white;
  font-size: 1.2em;
}

.row {
  display: flex;
  margin-bottom: 0px;
  flex-direction: row;
  justify-content: center;
  width: 100%;
}


.corridor {
  width: 50px;
  height: 50px;
  background-color: #abc3de;
}


.window {
  width: 10px;
  height: 40px;
  border-radius: 5px;
  background-color: #d0d0d0;
}

.engine {
  width: 50px;
  height: 50px;
  background-color: #5b4141;
}

.emergency {
  width: 20px;
  height: 40px;
  border-radius: 5px;
  background-color: #ffcccc;
}

.emergencyexitleft, .emergencyexitright {
  width: 50px;
  height: 50px;
  background-color: #b67c89;
}
.emergencyblank{
  width: 50px;
  height: 50px;
  background-color: #b67c89;
}

.toilet {
  width: 50px;
  height: 50px;
  background-color: #cce5ff;
}

.blank {
  width: 50px;
  height: 50px;
  background-color: #f0f0f0;
}

.door {
  width: 50px;
  height: 50px;
  background-color: #cccccc;
}

.occupied {
  background-color: #fc6c2b;
  cursor: not-allowed;
}

.selected {
  border-color: #007BFF;
  background-color: #D0E7FF !important;
}

.suggested{
  background-color: #2ecc71;
}

/* Add more styles for other types like 'emergencyexitleft', 'wing', etc., as desired */
</style>
