#ifndef LED_ARRAY_H_
#define LED_ARRAY_H_

#include "led_strip.h"

#define ROW_COUNT 18
#define COL_COUNT 30

typedef struct {
  color_t color;
  uint8_t row_start;
  uint8_t col_start;
  uint8_t row_end;
  uint8_t col_end;
} window_t;

#ifdef __cplusplus
extern "C"{
#endif

void led_window(led_strip_t *strip, window_t *window);

#ifdef __cplusplus
}
#endif

#endif
