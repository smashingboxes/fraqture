#include <SPI.h>
#include "led_strip.h"
#include "led_array.h"
#include "terminal.h"

terminal_t terminal;
led_strip_t strip;

typedef struct __attribute__((__packed__)) {
  uint16_t index;
  color_t color;
} set_packet_t;

SPISettings spi_settings(2000000, MSBFIRST, SPI_MODE0); 

void clear_led_f(uint16_t index, color_t *color)
{
  memset(color, 0, sizeof(color_t));
}

void clear_leds(void *_none)
{
  led_map(&strip, clear_led_f);
}

void window_leds(void *window)
{
  window_t *cast_window = (window_t *)window;
  led_window(&strip, cast_window);
}

void set_leds(void *packet)
{
  set_packet_t *cast_set = (set_packet_t *)packet;
  led_set(&strip, cast_set->index, &cast_set->color);
}

void refresh_leds(void *_none) 
{
  led_refresh(&strip);
}

terminal_cmd_t cmd_clear = {
  .trigger = 'C',
  .length = 0,
  .handler = clear_leds,
  .next = NULL
};

terminal_cmd_t cmd_refresh = {
  .trigger = 'R',
  .length = 0,
  .handler = refresh_leds,
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
}

void led_start(void) {
  SPI.beginTransaction(spi_settings);
}

void led_stop(void) {
  SPI.endTransaction();
}

void setup() {
  SPI.begin();
  Serial.begin(9600);
  led_init(&strip, led_transfer, led_start, led_stop);
  clear_leds(NULL);
  terminal_init(&terminal);
  terminal_attach(&terminal, &cmd_clear);
  terminal_attach(&terminal, &cmd_window);
  terminal_attach(&terminal, &cmd_set);
  terminal_attach(&terminal, &cmd_refresh);
}

void loop() {
  if(Serial.available()) terminal_feed(&terminal, Serial.read(), millis());
}
