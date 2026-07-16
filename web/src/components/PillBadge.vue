<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  type: 'module' | 'action' | 'result' | 'neutral' | 'success' | 'fail'
  value: string
}>()

const badgeClass = computed(() => {
  switch (props.type) {
    case 'result':
    case 'success':
      return 'badge badge-success'
    case 'fail':
      return 'badge badge-fail'
    case 'module':
      return 'badge badge-accent'
    case 'action':
      return 'badge badge-neutral'
    default:
      return 'badge badge-neutral'
  }
})
</script>

<template>
  <span :class="badgeClass">{{ value }}</span>
</template>
