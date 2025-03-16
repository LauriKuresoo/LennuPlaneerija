import {defineStore} from "pinia";

export const useBookingStore = defineStore("booking", {
    state: () => ({
        nrOfPeople: Number,
        preferences: []
    }),
    actions: {
        setBooking(nr, prefs){
            this.nrOfPeople = nr;
            this.preferences = prefs
        }
    }
})
