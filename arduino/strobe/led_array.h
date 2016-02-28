#ifndef LED_ARRAY_H_
#define LED_ARRAY_H_

#include "led_strip.h"

#define ROW_COUNT 18
#define COL_COUNT 30

typedef struct __attribute__((__packed__)) {
  uint8_t row_start;
  uint8_t col_start;
  uint8_t row_end;
  uint8_t col_end;
  color_t color;
} window_t;

#ifdef __cplusplus
extern "C"{
#endif

void led_window(led_strip_t *strip, window_t *window);

uint16_t led_position_to_index(uint8_t row, uint8_t col);

#ifdef __cplusplus
}
#endif

#endif
