import {defineStore} from "pinia";

export const useColorStore = defineStore("colors", {
    state: () => ({
        occupied: "#fc6c2b",
        freeSeat: "#ffffff",
        selected: "#a8cbf8",
        suggested: "#2ecc71",
    }),
    actions: {
        setColors(occupied, freeSeat, selected, suggested){
            this.occupied = occupied;
            this.freeSeat = freeSeat;
            this.selected = selected;
            this.suggested = suggested;
        }
    }
})

