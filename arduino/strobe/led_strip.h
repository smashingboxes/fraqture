#ifndef LED_STRIP_H_
#define LED_STRIP_H_

#include "color.h"

// Our installation has 18 meters of LED at 30 LEDs per meter (540 total), but
// this can be anything up to 65536
#ifndef LED_COUNT
#define LED_COUNT (19 * 30)
#endif

#define LED_BRIGHTNESS 0.5

// Function definition for mapping over the LEDs
// index, color
typedef void (*led_map_f)(uint16_t, color_t *);

// A write function for this library to use
typedef void (*led_write_f)(uint8_t);

// A transaction start/stop
typedef void (*led_void_f)(void);

typedef struct {
  led_void_f start;
  led_void_f stop;
  led_write_f write;
  color_t leds[LED_COUNT];
} led_strip_t;

#ifdef __cplusplus
extern "C"{
#endif

void led_init(
  led_strip_t *strip,
  led_write_f write_func,
  led_void_f start_func,
  led_void_f stop_func
);
void led_refresh(led_strip_t *strip);
void led_set(led_strip_t *strip, uint16_t index, color_t *color);
void led_map(led_strip_t *strip, led_map_f map_func);

#ifdef __cplusplus
}
#endif

#endif
