import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/global.css'

/* FontAwesome */
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import {
  faChartBar,
  faCheck,
  faTimes,
  faBars,
  faClock,
  faTableCells,
  faChevronLeft,
  faChevronRight,
  faExclamationTriangle,
  faFileExport,
  faSearch,
  faCircle,
  faCircleDot,
  faArrowUp,
  faArrowRight,
  faUser,
  faLink,
  faBox,
  faGauge,
  faEquals,
  faStopwatch,
  faLayerGroup,
  faLightbulb,
  faChartLine,
  faShieldHalved,
  faShield,
  faCheckCircle,
} from '@fortawesome/free-solid-svg-icons'

library.add(
  faChartBar,
  faCheck,
  faTimes,
  faBars,
  faClock,
  faTableCells,
  faChevronLeft,
  faChevronRight,
  faExclamationTriangle,
  faFileExport,
  faSearch,
  faCircle,
  faCircleDot,
  faArrowUp,
  faArrowRight,
  faUser,
  faLink,
  faBox,
  faGauge,
  faEquals,
  faStopwatch,
  faLayerGroup,
  faLightbulb,
  faChartLine,
  faShieldHalved,
  faShield,
  faCheckCircle,
)

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.component('FontAwesomeIcon', FontAwesomeIcon)
app.mount('#app')