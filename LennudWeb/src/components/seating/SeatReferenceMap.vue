<script setup>
import {useColorStore} from "@/stores/colorStore.js";
import {onMounted} from "vue";

const colorStore = useColorStore();

onMounted(() => {
  console.log("Component mounted.", useColorStore.occupied);
})
</script>

<template>
  <div class="big-box">
    <div class="row-container">
      <div class="color-box" :style="{backgroundColor: colorStore.occupied}"/>
      <h3> Occupied Seats</h3>
    </div>
    <div class="row-container">
      <div class="color-box" :style="{backgroundColor: colorStore.suggested}"/>
      <h3> Suggested Seats</h3>
    </div>
    <div class="row-container">
      <div class="color-box" :style="{backgroundColor: colorStore.selected}"/>
      <h3> Selected Seats</h3>
    </div>
    <div class="row-container">
      <div class="color-box" :style="{backgroundColor: colorStore.freeSeat}"/>
      <h3> Free Seats</h3>
    </div>
  </div>
</template>

<style scoped>
  .big-box{
    display: flex;
    flex-direction: column;
    margin-bottom: 20px;
  }
  .row-container{
    display: flex;
    flex-direction: row;
    gap: 1rem;
    margin-top: 1rem;
  }
  .color-box{
    width: 30px ;
    height: 30px ;
    border-radius: 5px;
  }
  h3{

  }

</style>
