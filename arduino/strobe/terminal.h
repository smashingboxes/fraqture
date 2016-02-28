#ifndef TERMINAL_H_
#define TERMINAL_H_

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>

// Number of milliseconds between character receives that will trigger a comms reset
#define RESET_TIMEOUT 2000
#define CMD_LENGTH 10

typedef void (*terminal_handler)(void *);

typedef struct terminal_cmd {
  char trigger;
  uint8_t length;
  terminal_handler handler;
  struct terminal_cmd *next;
} terminal_cmd_t;

typedef struct {
  terminal_cmd_t *current_handler;
  terminal_cmd_t *handler_chain;
  uint8_t character_index;
  uint32_t last_receive;
  char buffer[CMD_LENGTH];
} terminal_t;

#ifdef __cplusplus
extern "C"{
#endif

void terminal_init(terminal_t *term);
void terminal_attach(terminal_t *term, terminal_cmd_t *cmd);
void terminal_feed(terminal_t *term, char incoming, uint32_t millis);

#ifdef __cplusplus
}
#endif

#endif
