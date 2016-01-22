#include "led_strip.h"
#include <string.h>

void led_write(led_strip_t *strip, color_t *color);

void led_init(led_strip_t *strip, led_write_f write_func)
{
  color_t black = { 0, 0, 0 };
  uint16_t x;
  for(x = 0; x < LED_COUNT; x++) memcpy(&strip->leds[x], &black, sizeof(color_t));
  strip->write = write_func;
}

void led_refresh(led_strip_t *strip)
{
  uint16_t x;
  for(x = 0; x < 4; x++) strip->write(0);
  for(x = 0; x < LED_COUNT; x++) led_write(strip, &strip->leds[x]);
}

void led_set(led_strip_t *strip, uint16_t index, color_t *color)
{
  memcpy(&strip->leds[index], color, sizeof(color_t));
}

void led_map(led_strip_t *strip, led_map_f map_func)
{
  uint16_t x;
  for(x = 0; x < LED_COUNT; x++) map_func(x, &strip->leds[x]);
}

void led_write(led_strip_t *strip, color_t *color) {
  strip->write(0xFF);
  strip->write(color->r);
  strip->write(color->g);
  strip->write(color->b);
}

