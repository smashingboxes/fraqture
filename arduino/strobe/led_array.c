#include "led_array.h"
#include "color.h"

static uint16_t position_to_index(uint8_t row, uint8_t col);

void led_window(led_strip_t *strip, window_t *window)
{
  uint8_t row = window->row_start;
  uint8_t col = window->col_start;
  for (row = window->row_start; row < window->row_end; row++) {
    for (col = window->col_start; col < window->col_end; col++) {
      uint16_t index = position_to_index(row, col);
      color_t output_color;
      color_t black;
      black.r = 0;
      black.b = 0;
      black.g = 0;
      lerp_color(&window->color, &black, 0.5, &output_color);
      led_set(strip, index, &window->color);
    }
  }
}

static uint16_t position_to_index(uint8_t row, uint8_t col)
{
  if(row > 17 || col > 29) return 0;
 
  uint16_t offset;
  if(row > 8) {
    offset = 540 - 1;
    row -= 9;
  } else {
    offset = 270 - 1;
  }

  if(row % 2 == 0) return offset - (row * COL_COUNT + col);  
  return offset - ((1 + row) * COL_COUNT - col - 1);
}
