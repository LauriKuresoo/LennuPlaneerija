import { createRouter, createWebHistory } from 'vue-router'
import FlightList from '@/components/FlightList.vue'
import SeatSelector from '@/components/SeatSelector.vue'

const routes = [
    { path: '/', component: FlightList },
    { path: '/seats', component: SeatSelector},
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
