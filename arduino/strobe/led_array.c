#include "led_array.h"

static uint16_t position_to_index(uint8_t row, uint8_t col);

void led_window(led_strip_t *strip, window_t *window)
{
  uint8_t row = window->row_start;
  uint8_t col = window->col_start;
  for (row = window->row_start; row < window->row_end; row++) {
    for (col = window->col_start; col < window->col_end; col++) {
      uint16_t index = position_to_index(row, col);
      led_set(strip, index, &window->color);
    }
  }
}

static uint16_t position_to_index(uint8_t row, uint8_t col)
{
  if(row % 2 == 0) return row * COL_COUNT + col;  
  return (1 + row) * COL_COUNT - col - 1;
}
