<script setup>
import {computed, onUpdated, reactive, ref} from "vue";

const props = defineProps({seat: Object, styleProps: Object})

const prevProps = ref({ ...props });

onUpdated(() => {
  console.log('Component updated');

  // Compare previous and current props
  if (prevProps.value.selected !== props.selected) {
    console.log(`selected changed from ${prevProps.value.selected} to ${props.selected}`);
  }
  if (prevProps.value.seat.occupied !== props.seat.occupied) {
    console.log(`occupied changed from ${prevProps.value.seat.occupied} to ${props.seat.occupied}`);
  }

  // Update the previous props reference for next update
  prevProps.value = { ...props };
});

const seatClass = computed(() => ({
  seat: true,
  occupied: props.seat.occupied,
  selected: props.styleProps.selected,
  suggested: props.styleProps.suggested,
}));

</script>

<template>
  <div class="seatBox">
    <div :class="seatClass">{{props.seat.seatNumber}}</div>
  </div>
</template>

<style scoped>
.seatBox{
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
}
.seat{
  width: 80%;
  height: 80%;
  padding: 0px 10px 0px 10px;
  display: flex;
  border-radius: 5px;
  justify-content: center;
  align-items: center;
  border: 1px solid #ccc;
  cursor: pointer;
}

/* Styles for non-seat elements */
.non-seat {
  background-color: #eee; /* Default background for non-seats */
}
</style>
