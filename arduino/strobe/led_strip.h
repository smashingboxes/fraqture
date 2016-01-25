#ifndef LED_STRIP_H_
#define LED_STRIP_H_

#include "color.h"

// Default the LED_COUNT to 2m of 60 led/m
#ifndef LED_COUNT
#define LED_COUNT 120
#endif

// Function definition for mapping over the LEDs
// index, color
typedef void (*led_map_f)(uint16_t, color_t *);

// A write function for this library to use
typedef void (*led_write_f)(uint8_t);

typedef struct {
  led_write_f write;
  color_t leds[LED_COUNT];
} led_strip_t;

#ifdef __cplusplus
extern "C"{
#endif

void led_init(led_strip_t *strip, led_write_f write_func);
void led_refresh(led_strip_t *strip);
void led_set(led_strip_t *strip, uint16_t index, color_t *color);
void led_map(led_strip_t *strip, led_map_f map_func);

#ifdef __cplusplus
}
#endif

#endif
