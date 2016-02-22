#include <SPI.h>
#include "led_strip.h"
#include "terminal.h"

terminal_t terminal;
led_strip_t strip;

typedef struct {
  uint16_t index;
  color_t color;
} set_packet_t;

void clear_led_f(uint16_t index, color_t *color)
{
  memset(color, 0, sizeof(color_t));
}

void clear_leds(void *_none)
{
  led_map(&strip, clear_led_f);
  led_refresh(&strip);
}

void window_leds(void *window)
{
  window_t cast_window = (window_t *)window;
  led_window(&strip, window);
  led_refresh(&strip);
}

void set_leds(void *set)
{
  set_packet_t cast_set = (set_packet_t *)set;
  led_set(&strip, set->index, &set->color);
  led_refresh(&strip);
}

terminal_cmd_t cmd_clear = {
  .trigger = 'C',
  .length = 0,
  .handler = clear_leds,
  .next = NULL
};

terminal_cmd_t cmd_window = {
  .trigger = 'W',
  .length = sizeof(window_t),
  .handler = window_leds,
  .next = NULL
};

terminal_cmd_t cmd_set = {
  .trigger = 'S',
  .length = sizeof(set_packet_t),
  .handler = set_leds,
  .next = NULL
};

void led_transfer(uint8_t x) {
  SPI.transfer(x);
  Serial.begin(9600);
}

void setup() {
  SPI.begin();
  led_init(&strip, led_transfer);
  clear_leds(NULL);
  terminal_init(&terminal);
  terminal_attach(&terminal, &cmd_clear);
  terminal_attach(&terminal, &cmd_window);
  terminal_attach(&terminal, &cmd_set);
}

void loop() {
  if(Serial.available()) terminal_feed(&terminal, Serial.read(), millis());
}
